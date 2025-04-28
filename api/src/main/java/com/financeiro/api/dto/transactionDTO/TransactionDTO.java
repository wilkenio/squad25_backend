package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.TransactionType;

public record TransactionDTO(
    Double value,
    TransactionType type
) {
    
}
