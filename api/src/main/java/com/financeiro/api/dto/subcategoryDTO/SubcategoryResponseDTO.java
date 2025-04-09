package com.financeiro.api.dto.subcategoryDTO;

import com.financeiro.api.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubcategoryResponseDTO(
        UUID id,
        String name,
        Boolean standardRecommendation,
        UUID categoryId,
        String iconClass,
        Status status,
        String color,
        String additionalInfo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
