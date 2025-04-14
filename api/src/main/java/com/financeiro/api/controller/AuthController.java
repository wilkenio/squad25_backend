package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.dto.authDTO.ApiResponse;
import com.financeiro.api.dto.authDTO.LoginRequestDTO;
import com.financeiro.api.dto.authDTO.RegisterRequestDTO;
import com.financeiro.api.dto.authDTO.ResponseDTO;
import com.financeiro.api.infra.security.TokenService;
import com.financeiro.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private final String RECAPTCHA_SECRET_KEY = "6LeuaOYqAAAAACs9m3ysAu2cPbZD5ft0cqIwrf4d"; // Chave secreta do reCAPTCHA

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // Validação do reCAPTCHA usando a API do Google
    private boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify?secret=" + RECAPTCHA_SECRET_KEY + "&response=" + recaptchaToken;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        return result != null && result.contains("\"success\": true");
    }

    // Endpoint de login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseDTO>> login(@RequestBody LoginRequestDTO body) {
        try {
            //Validação do reCAPTCHA (opcional)
            // if (body.recaptchaToken() != null && !validateRecaptcha(body.recaptchaToken())) {
            //     return ResponseEntity.badRequest().body(new ApiResponse<>(400, "reCAPTCHA inválido"));
            // }

            User user = userRepository.findByEmail(body.email()).orElse(null);
            if (user == null || !passwordEncoder.matches(body.password(), user.getPassword())) {
                return ResponseEntity.status(401).body(new ApiResponse<>(401, "Credenciais inválidas"));
            }

            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new ApiResponse<>(200, new ResponseDTO(user.getName(), token)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Erro interno no servidor"));
        }
    }

    // Endpoint de registro
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ResponseDTO>> register(@RequestBody RegisterRequestDTO body) {
        try {
            // Validação do reCAPTCHA (opcional)
            if (body.recaptchaToken() != null && !validateRecaptcha(body.recaptchaToken())) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "reCAPTCHA inválido"));
            }

            if (userRepository.findByEmail(body.email()).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Email já cadastrado"));
            }

            User newUser = new User();
            newUser.setName(body.name());
            newUser.setEmail(body.email());
            newUser.setPassword(passwordEncoder.encode(body.password()));
            userRepository.save(newUser);

            String token = tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ApiResponse<>(200, new ResponseDTO(newUser.getName(), token)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Erro interno no servidor"));
        }
    }

    // Endpoint de logout (apenas informativo, token é descartado no client)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Logout efetuado no client. Token descartado."));
    }
}