package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryListDTO(
    UUID categoryId,
    String name,
    CategoryType type,
    String iconClass,
    BigDecimal value
) {}