package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.dto.authDTO.ApiResponse;
import com.financeiro.api.dto.authDTO.LoginRequestDTO;
import com.financeiro.api.dto.authDTO.RegisterRequestDTO;
import com.financeiro.api.dto.authDTO.ResponseDTO;
import com.financeiro.api.infra.exceptions.InvalidCredentialsException;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.infra.security.TokenService;
import com.financeiro.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String RECAPTCHA_SECRET_KEY = "6LeuaOYqAAAAACs9m3ysAu2cPbZD5ft0cqIwrf4d";  // Substitua pela sua chave secreta do reCAPTCHA

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // Método para validar o reCAPTCHA
    private boolean validateRecaptcha(String recaptchaToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify?secret=" + RECAPTCHA_SECRET_KEY + "&response=" + recaptchaToken;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        return result != null && result.contains("\"success\": true");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseDTO>> login(@RequestBody LoginRequestDTO body, HttpServletResponse response) {
        // Validar reCAPTCHA
        if (!validateRecaptcha(body.recaptchaToken())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Invalid reCAPTCHA"));
        }
    
        // Buscar o usuário no banco de dados
        User user = userRepository.findByEmail(body.email()).orElse(null);
    
        // Validar senha de forma segura
        if (user == null || !passwordEncoder.matches(body.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Credenciais inválidas"));
        }
    
        // Gerar o token JWT
        String token = tokenService.generateToken(user);
    
        // Criar o cookie com o token
        Cookie cookie = new Cookie("JWT", token); 
        cookie.setHttpOnly(true); 
        cookie.setSecure(true); 
        cookie.setPath("/"); 
        cookie.setMaxAge(86400); 
    
        // Adicionar o cookie na resposta
        response.addCookie(cookie);
    
        return ResponseEntity.ok(new ApiResponse<>(200, new ResponseDTO(user.getName(), "Token set in cookie")));
    }
    

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ResponseDTO>> register(@RequestBody RegisterRequestDTO body, HttpServletResponse response) {
        try {
            // Validar reCAPTCHA
            if (!validateRecaptcha(body.recaptchaToken())) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Invalid reCAPTCHA"));
            }
    
            // Verificar se o email já está registrado
            if (userRepository.findByEmail(body.email()).isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Email already registered"));
            }
    
            // Criar um novo usuário
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
    
            // Salvar no banco
            userRepository.save(newUser);
    
            // Gerar o token JWT
            String token = tokenService.generateToken(newUser);
    
            // Criar o cookie com o token
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);
    
            return ResponseEntity.ok(new ApiResponse<>(200, new ResponseDTO(newUser.getName(), "Token set in cookie")));
        } catch (Exception e) {
            e.printStackTrace();  // Imprime o erro no console
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Internal Server Error"));
        }
    }
    

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        // Criar o cookie JWT com o mesmo nome do cookie de login
        Cookie cookie = new Cookie("JWT", null); 
        cookie.setHttpOnly(true); 
        cookie.setSecure(true); 
        cookie.setPath("/"); 
        cookie.setMaxAge(0); 

        // Adicionar o cookie na resposta para removê-lo do cliente
        response.addCookie(cookie);

        return ResponseEntity.ok(new ApiResponse<>(200, "Successfully logged out"));
    }
}
