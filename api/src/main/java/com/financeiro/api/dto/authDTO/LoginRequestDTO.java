package com.financeiro.api.dto.authDTO;

public record LoginRequestDTO(String email, String password,String recaptchaToken) {
}
