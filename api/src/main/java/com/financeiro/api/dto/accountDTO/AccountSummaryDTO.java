package com.financeiro.api.dto.accountDTO;

public record AccountSummaryDTO(
    String accountName,
    Double income,
    Double expectedIncomeMonth,
    Double expense,
    Double expectedExpenseMonth
) {}
