package com.financeiro.api.dto.cardDTO;

import com.financeiro.api.domain.enums.Status;

public record CardRequestDTO(
    String name,
    Status status
) {}
