package com.financeiro.api.dto.dashboardDTO;

import java.util.UUID;

public record AccountBalanceDashboardDTO(
    UUID accountId,         // ID da conta (pode ser null para saldo consolidado)
    String accountName,     // Nome da conta ou "Saldo Consolidado de Contas"
    Double balance,         // O valor do saldo
    String balanceDescription, // Descrição: "Saldo Atual", "Saldo Total (Previsto + Inicial)", etc.
    String itemType
) implements DashboardItemDTO {

    // Construtor para facilitar a criação e definir o itemType automaticamente
    public AccountBalanceDashboardDTO(UUID accountId, String accountName, Double balance, String balanceDescription) {
        this(accountId, accountName, balance, balanceDescription, "ACCOUNT_BALANCE");
    }

    @Override
    public String getItemType() {
        return this.itemType;
    }
}