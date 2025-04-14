package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;

public record CategoryRequestDTO(
        String name,
        CategoryType type,
        String iconClass,
        String color,
        String additionalInfo,
        boolean standardRecommendation,
        Status status
) {}