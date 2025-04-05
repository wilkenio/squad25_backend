package com.financeiro.api.dto.userDTO;

public record UserRequestDTO(
    String name,
    String email,
    String password
) {}
