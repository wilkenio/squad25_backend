package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.enums.Status;

public record AccountTransactionResponseDTO(
        String categoryName,
        String iconClass,
        String color,
        String accountName,
        String accountDescription,
        Double openingBalance,
        Double currentBalance,
        Double expectedBalance,
        Double specialCheck,
        Double income,
        Double expense,
        Double expectedIncomeMonth,
        Double expectedExpenseMonth,
        Status status) {
}
