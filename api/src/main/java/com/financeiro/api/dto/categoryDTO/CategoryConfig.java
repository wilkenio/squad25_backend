package com.financeiro.api.dto.categoryDTO;

import java.util.List;

import com.financeiro.api.domain.enums.CategoryType;

public record CategoryConfig(String name,
    String icon, 
    String color, 
    CategoryType type, 
    List<String> subcategories) {}
