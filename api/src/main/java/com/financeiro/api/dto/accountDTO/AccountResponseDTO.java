package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.enums.Status;
import java.util.UUID;

public record AccountResponseDTO(
<<<<<<< HEAD
    UUID id,
    UUID userId,
    String accountName,
    UUID categoryId,
    String accountDescription,
    String additionalInformation,
    Double openingBalance,
    Double specialCheck,
    Status status
) {}
=======
        UUID id,
        UUID userId,
        String accountName,
        String accountDescription,
        String additionalInformation,
        Double openingBalance,
        Double specialCheck,
        Status status
) {}
>>>>>>> c25b5ec08d1d4d0be4685b699f44247235e52d2c
