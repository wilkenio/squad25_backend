package com.financeiro.api.dto.subcategoryDTO;

import java.util.UUID;

import com.financeiro.api.domain.enums.Status;

public record SubcategorySummaryDTO(
    UUID id,
    String name,
    Boolean standardRecommendation,
    String iconClass,
    Status status,
    String color,
    String additionalInfo
) {}
