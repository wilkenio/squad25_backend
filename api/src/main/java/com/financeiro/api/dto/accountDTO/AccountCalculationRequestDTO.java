package com.financeiro.api.dto.accountDTO;

import java.util.List;

import com.financeiro.api.dto.transactionDTO.TransactionDTO;

public record AccountCalculationRequestDTO(
        String accountName,
        String accountDescription,
        String additionalInformation,
        Double openingBalance,
        Integer openingBalanceMonth,
        Double specialCheck,
        String categoryName,
        List<TransactionDTO> transactions
) {
}
