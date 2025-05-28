package com.financeiro.api.dto.dashboardDTO;

import com.financeiro.api.domain.enums.Status;

import java.util.UUID;

public record DashboardRequestDTO(
        UUID id,
        String url,
        String name,
        String graficType,
        Status status
) {
}
