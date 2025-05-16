package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRequestDTO(
        UUID accounId,
        UUID categoryId,
        UUID subcategoryId,
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
        Boolean businessDayOnly         
) {}
