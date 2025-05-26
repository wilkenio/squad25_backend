package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID accountId,
        String accountName,
        UUID categoryId,
        String categoryName,
        UUID subcategoryId,
        String subcategoryName,
        String name,
        TransactionType type,
        Status status,
        LocalDateTime releaseDate,
        Double value,
        String description,
        TransactionState state,
        String additionalInformation,
        Frequency frequency,
        Integer installments,
        Periodicity periodicity,
        Boolean businessDayOnly,
        Integer installmentNumber,
        UUID recurringGroupId,
        UUID transferGroupId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean saldoNegativo
) {}