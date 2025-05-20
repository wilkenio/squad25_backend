package com.financeiro.api.dto.categoryDTO;

import java.util.UUID;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;

public record CategorySummaryDTO(
    UUID id,
    String name,
    CategoryType type,
    String iconClass,
    String color,
    String additionalInfo,
    Status status
) {}
