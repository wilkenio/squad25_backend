package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.enums.Status;
import java.util.UUID;

public record AccountRequestDTO(
    UUID userId,
    String accountName,
    UUID categoryId,
    String accountDescription,
    String additionalInformation,
    Double openingBalance,
    Double specialCheck,
    Status status
) {}
