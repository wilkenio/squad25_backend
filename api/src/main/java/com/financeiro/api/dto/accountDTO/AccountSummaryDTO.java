package com.financeiro.api.dto.accountDTO;

import java.util.UUID;
import com.financeiro.api.domain.enums.Status;

public record AccountSummaryDTO(
    UUID id,
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
    Status status
) {}
