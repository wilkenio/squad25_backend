package com.financeiro.api.dto.transferDTO;

import com.financeiro.api.domain.enums.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record TransferRequestDTO(
        UUID originAccountId,
        UUID destinationAccountId,
        String name,
        Double value,
        LocalDateTime releaseDate,
        LocalTime releaseTime,
        String description,
        String additionalInformation,
        TransactionState state,
        Frequency frequency,
        Integer installments,
        Periodicity periodicity,
        Boolean businessDayOnly
) {}
