package com.financeiro.api.dto.categoryDTO;

import java.math.BigDecimal;

public record CategoryListDTO(
    String iconClass,
    String name,
    BigDecimal value
) {}