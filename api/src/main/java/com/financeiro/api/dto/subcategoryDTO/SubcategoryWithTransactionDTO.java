package com.financeiro.api.dto.subcategoryDTO;

import java.math.BigDecimal;

public record SubcategoryWithTransactionDTO(
    String name,
    String iconClass,
    BigDecimal totalValue
) {}