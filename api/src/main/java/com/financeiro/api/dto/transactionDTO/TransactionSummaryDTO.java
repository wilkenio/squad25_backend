package com.financeiro.api.dto.transactionDTO;

import java.time.LocalDateTime;
import java.util.UUID;
import com.financeiro.api.domain.enums.*;

import com.financeiro.api.domain.enums.TransactionType;

public record TransactionSummaryDTO(
    UUID id,
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
    UUID recurringGroupId
) {}
