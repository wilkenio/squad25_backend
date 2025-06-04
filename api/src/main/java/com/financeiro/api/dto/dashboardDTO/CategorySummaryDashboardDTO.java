package com.financeiro.api.dto.dashboardDTO;

import java.util.UUID;

public record CategorySummaryDashboardDTO(
    UUID categoryId,
    String categoryName,
    Double totalValue,
    String iconClass, // Novo campo
    String color,     // Novo campo
    String itemType
) implements DashboardItemDTO {

    // Construtor principal para uso no serviço
    public CategorySummaryDashboardDTO(UUID categoryId, String categoryName, Double totalValue, String iconClass, String color) {
        this(categoryId, categoryName, totalValue, iconClass, color, "CATEGORY_SUMMARY");
    }
    
    // Construtor antigo para casos onde ícone/cor não são primários (ex: soma total)
    public CategorySummaryDashboardDTO(UUID categoryId, String categoryName, Double totalValue) {
        this(categoryId, categoryName, totalValue, null, null, "CATEGORY_SUMMARY");
    }

    @Override
    public String getItemType() {
        return this.itemType;
    }
}