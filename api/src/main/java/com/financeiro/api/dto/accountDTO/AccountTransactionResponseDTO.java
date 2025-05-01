package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.enums.Status;
import java.time.LocalDateTime;

public record AccountTransactionResponseDTO(
        String accountName,
        String accountDescription,
        String additionalInformation,
        Double openingBalance,
        Double currentBalance,
        Double expectedBalance,
        Double specialCheck,
        Double income,
        Double expense,
        Double expectedIncomeMonth,
        Double expectedExpenseMonth,
        Status status,
        Category category,
        LocalDateTime updatedAt) {
}
