package com.financeiro.api.dto.dashboardDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionDashboardDTO(
    UUID transactionId,
    String name,
    LocalDateTime date,         
    Double value,
    String state,
    String transactionType,
    String accountName,
    String accountIconClass, 
    String accountColor,
    String categoryName,
    String categoryIconClass, 
    String categoryColor,     
    String subcategoryName,   
    String itemType
) implements DashboardItemDTO {

    // Construtor principal para uso no serviço
    public TransactionDashboardDTO(UUID transactionId, String name, LocalDateTime date, Double value, 
                                   String transactionType, String state, String accountName, 
                                   String accountIconClass, String accountColor, String categoryName, 
                                   String categoryIconClass, String categoryColor, String subcategoryName) {
        this(transactionId, name, date, value, transactionType, state, accountName, 
            accountIconClass, accountColor, categoryName,  
            categoryIconClass, categoryColor, subcategoryName, "TRANSACTION_DETAIL");
    }

    // Construtor antigo para manter compatibilidade se necessário, mas o novo é preferível
    public TransactionDashboardDTO(UUID transactionId, String name, LocalDateTime date, 
                                    Double value, String transactionType, String state, String accountName, String categoryName) {
        this(transactionId, name, date, value, transactionType, state, 
            accountName, categoryName, null, null, null, null, null, "TRANSACTION_DETAIL");
    }


    @Override
    public String getItemType() {
        return this.itemType;
    }
}