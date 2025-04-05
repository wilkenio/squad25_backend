package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        UUID userId,
        String name,
        CategoryType type,
        String iconClass,
        boolean standardRecommendation,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}