package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;

import java.util.UUID;

public record CategoryListDTO(
    UUID categoryId,
    String name,
    CategoryType type,
    String iconClass,
    String color,
    Double value
) {}