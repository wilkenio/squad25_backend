package com.financeiro.api.dto.accountDTO;

import java.util.UUID;

public record AccountCalculationRequestDTO(
        String accountName,
        String accountDescription,
        String additionalInformation,
        Double openingBalance,
        Double specialCheck,
        UUID categoryId
) {}
