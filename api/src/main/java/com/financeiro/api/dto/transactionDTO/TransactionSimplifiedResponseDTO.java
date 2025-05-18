package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.domain.enums.Frequency;

import java.time.LocalDateTime;

public record TransactionSimplifiedResponseDTO(
        String name,
        TransactionType type,
        String accountName,
        LocalDateTime releaseDate,
        Frequency frequency,
        Double value) {
}