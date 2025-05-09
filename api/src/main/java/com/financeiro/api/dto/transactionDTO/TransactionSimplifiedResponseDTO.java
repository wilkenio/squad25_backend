package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.domain.enums.Frequency;

import java.time.LocalDateTime;

public record TransactionSimplifiedResponseDTO(
        String name,
        String description,
        TransactionType type,
        String accountName,
        Frequency frequency,
        Double value) {
}