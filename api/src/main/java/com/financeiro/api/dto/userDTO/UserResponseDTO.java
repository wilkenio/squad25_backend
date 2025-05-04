package com.financeiro.api.dto.userDTO;

import com.financeiro.api.domain.enums.Status;

import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    Status status
) {}
