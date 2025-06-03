package com.financeiro.api.dto.categoryDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;

public record CategoryResponseByIdDTO(
    UUID id,
    UUID userId,
    String name,
    CategoryType type,
    String iconClass,
    String color,
    String additionalInfo,
    boolean standardRecommendation,
    Status status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<SubcategoryResponseDTO> subcategories
) {}
