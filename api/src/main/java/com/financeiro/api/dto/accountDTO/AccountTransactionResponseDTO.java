package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.enums.Status;

import java.util.UUID;

public record AccountTransactionResponseDTO(
        UUID id,
        UUID categoryId,
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
