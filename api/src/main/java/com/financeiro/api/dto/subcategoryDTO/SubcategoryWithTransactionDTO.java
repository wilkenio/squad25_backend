package com.financeiro.api.dto.subcategoryDTO;

import com.financeiro.api.domain.enums.CategoryType;
import java.math.BigDecimal;
import java.util.UUID;

public record SubcategoryWithTransactionDTO(
    UUID id,
    String name,
    String iconClass,
    String color,
    CategoryType categoryType,
    BigDecimal totalValue
) {}