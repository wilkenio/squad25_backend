package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRequestDTO(
        UUID accountId,
        UUID categoryId,
        UUID subcategoryId,
        String name,
        TransactionType type,
        Status status,
        LocalDateTime releaseDate,
        BigDecimal value,
        String description,
        TransactionState state,
        String additionalInformation,
        Frequency frequency,
        Integer installments,
        Integer customDays
) {}
