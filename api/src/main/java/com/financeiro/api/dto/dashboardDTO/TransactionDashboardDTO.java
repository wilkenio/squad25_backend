package com.financeiro.api.dto.dashboardDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionDashboardDTO(
    UUID transactionId,
    String name,
    LocalDateTime date,         // Data da transação (que já é o releaseDate na sua implementação)
    Double value,
    String transactionType,
    String accountName,
    String categoryName,
    String state,
    String categoryIconClass, // Novo campo
    String categoryColor,     // Novo campo
    String subcategoryName,   // Novo campo
    String itemType
) implements DashboardItemDTO {

    // Construtor principal para uso no serviço
    public TransactionDashboardDTO(UUID transactionId, String name, LocalDateTime date, Double value, 
                                   String transactionType, String accountName, String categoryName, String state,
                                   String categoryIconClass, String categoryColor, String subcategoryName) {
        this(transactionId, name, date, value, transactionType, accountName, categoryName, state, 
             categoryIconClass, categoryColor, subcategoryName, "TRANSACTION_DETAIL");
    }

    // Construtor antigo para manter compatibilidade se necessário, mas o novo é preferível
    public TransactionDashboardDTO(UUID transactionId, String name, LocalDateTime date, Double value, String transactionType, String accountName, String categoryName, String state) {
        this(transactionId, name, date, value, transactionType, accountName, categoryName, state, null, null, null, "TRANSACTION_DETAIL");
    }


    @Override
    public String getItemType() {
        return this.itemType;
    }
}