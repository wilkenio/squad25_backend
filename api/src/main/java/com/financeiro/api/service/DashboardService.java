package com.financeiro.api.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.api.dto.dashboardDTO.DashboardRequestDTO;
import com.financeiro.api.dto.dashboardDTO.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO create(DashboardRequestDTO dto);
    DashboardResponseDTO update(UUID id, DashboardRequestDTO dto);
    List<DashboardResponseDTO> findAll();
    DashboardResponseDTO findById(UUID id);
    void delete(UUID id);
}
