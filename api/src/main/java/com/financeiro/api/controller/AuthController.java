package com.financeiro.api.controller;

import com.financeiro.api.dto.authDTO.ApiResponse;
import com.financeiro.api.dto.authDTO.LoginRequestDTO;
import com.financeiro.api.dto.authDTO.RegisterRequestDTO;
import com.financeiro.api.dto.authDTO.ResponseDTO;
import com.financeiro.api.dto.userDTO.UserRequestDTO;
import com.financeiro.api.dto.userDTO.UserResponseDTO;
import com.financeiro.api.domain.User;
import com.financeiro.api.infra.security.TokenService;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // Chave secreta do reCAPTCHA (opcional)
    private static final String RECAPTCHA_SECRET_KEY = 
        "6LeuaOYqAAAAACs9m3ysAu2cPbZD5ft0cqIwrf4d";

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // Validação do reCAPTCHA usando a API do Google
    private boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify" +
                     "?secret=" + RECAPTCHA_SECRET_KEY +
                     "&response=" + recaptchaToken;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        return result != null && result.contains("\"success\": true");
    }

    // Endpoint de login (mantido igual)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseDTO>> login(
            @RequestBody LoginRequestDTO body) {
        try {
            User user = userRepository.findByEmail(body.email()).orElse(null);
            if (user == null || 
                !passwordEncoder.matches(body.password(), user.getPassword())) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(401, "Credenciais inválidas"));
            }
            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, new ResponseDTO(user.getName(), token))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(500, "Erro interno no servidor"));
        }
    }

    // Endpoint de registro, agora usando UserService.create(...) :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ResponseDTO>> register(
            @RequestBody RegisterRequestDTO body) {
        try {
            // reCAPTCHA (opcional)
            if (body.recaptchaToken() != null && 
                !validateRecaptcha(body.recaptchaToken())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(400, "reCAPTCHA inválido"));
            }

            if (userRepository.findByEmail(body.email()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(400, "Email já cadastrado"));
            }

            // 1) Cria o usuário e clona os templates de categoria/subcategoria
            UserRequestDTO userReq = new UserRequestDTO(
                    body.name(), 
                    body.email(), 
                    body.password()
            );
            UserResponseDTO created = userService.create(userReq);

            // 2) Recupera a entidade para gerar o token
            User userEntity = userRepository
                    .findByEmail(created.email())
                    .orElseThrow(() -> 
                        new RuntimeException("Usuário recém-criado não encontrado")
                    );

            // 3) Gera JWT e retorna nome + token
            String token = tokenService.generateToken(userEntity);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, 
                        new ResponseDTO(created.name(), token))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(500, "Erro interno no servidor"));
        }
    }

    // Endpoint de logout (permanece igual)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(
                new ApiResponse<>(200, 
                    "Logout efetuado no client. Token descartado.")
        );
    }
}
