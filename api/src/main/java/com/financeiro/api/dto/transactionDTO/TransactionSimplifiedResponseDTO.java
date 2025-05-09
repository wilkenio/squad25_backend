package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.domain.enums.Frequency;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionSimplifiedResponseDTO(
        UUID id,
        String name,
        String description,
        TransactionType type,
        String accountName,
        Frequency frequency,
        Double value) {
}