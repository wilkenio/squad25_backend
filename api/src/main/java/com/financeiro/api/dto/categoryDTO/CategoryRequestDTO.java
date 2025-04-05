package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;

import java.util.UUID;

public record CategoryRequestDTO(
        UUID userId,
        String name,
        CategoryType type,
        String iconClass,
        boolean standardRecommendation,
        Status status
) {}