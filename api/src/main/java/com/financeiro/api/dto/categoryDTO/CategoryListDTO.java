package com.financeiro.api.dto.categoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import java.util.UUID;

public record CategoryListDTO(
    UUID id,
    String name,
    CategoryType type,
    Status status
) {}