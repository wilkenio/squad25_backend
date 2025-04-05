package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.enums.Status;
import java.util.UUID;

public record AccountResponseDTO(
    UUID id,
    UUID userId,
    String accountName,
    String accountDescription,
    String additionalInformation,
    Double openingBalance,
    Double specialCheck,
    Status status
) {}
