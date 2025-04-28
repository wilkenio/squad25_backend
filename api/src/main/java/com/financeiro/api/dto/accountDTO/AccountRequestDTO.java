package com.financeiro.api.dto.accountDTO;

public record AccountRequestDTO(
    String accountName,
    String accountDescription,
    String additionalInformation,
    Double openingBalance,
    Double specialCheck
) {}
