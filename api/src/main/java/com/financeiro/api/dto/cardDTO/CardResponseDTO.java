package com.financeiro.api.dto.cardDTO;

import com.financeiro.api.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponseDTO(
    UUID id,
    String name,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
