package com.financeiro.api.dto.subcategoryDTO;

import java.util.UUID;

public record SubcategoryRequestDTO(
    String name,
    Boolean standardRecommendation,
    UUID categoryId,
    String iconClass
) {}
