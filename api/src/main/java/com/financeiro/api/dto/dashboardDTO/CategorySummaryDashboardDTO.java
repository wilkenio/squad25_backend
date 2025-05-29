package com.financeiro.api.dto.dashboardDTO;

import java.util.UUID;

public record CategorySummaryDashboardDTO(
    UUID categoryId,     // ID da categoria (null para "Sem Categoria" ou somas gerais)
    String categoryName,   // Nome da categoria ou "Sem Categoria" ou "Soma Total de X"
    Double totalValue,     // Valor total da categoria ou da soma
    String itemType
) implements DashboardItemDTO {

    public CategorySummaryDashboardDTO(UUID categoryId, String categoryName, Double totalValue) {
        this(categoryId, categoryName, totalValue, "CATEGORY_SUMMARY");
    }

    @Override
    public String getItemType() {
        return this.itemType;
    }
}
