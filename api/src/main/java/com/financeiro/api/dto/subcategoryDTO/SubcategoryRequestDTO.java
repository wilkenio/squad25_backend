package com.financeiro.api.dto.subcategoryDTO;

import com.financeiro.api.domain.enums.Status;

import java.util.UUID;

public record SubcategoryRequestDTO(
        String name,
        Boolean standardRecommendation,
        UUID categoryId,
        String iconClass,
        Status status,
        String color,
        String additionalInfo
) {}
