package com.financeiro.api.dto.dashboardDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionDashboardDTO(
    UUID transactionId,
    String name,                // Nome/descrição da transação
    LocalDateTime date,         // Data relevante (ex: data de lançamento ou vencimento)
    Double value,
    String transactionType,     // "RECEITA", "DESPESA", "TRANSFERENCIA"
    String accountName,
    String categoryName,        // Nome da categoria (pode ser null)
    String state,               // Estado da transação: "PENDENTE", "PAGO", etc.
    String itemType
) implements DashboardItemDTO {

    public TransactionDashboardDTO(UUID transactionId, String name, LocalDateTime date, Double value, String transactionType, String accountName, String categoryName, String state) {
        this(transactionId, name, date, value, transactionType, accountName, categoryName, state, "TRANSACTION_DETAIL");
    }

    @Override
    public String getItemType() {
        return this.itemType;
    }
}
