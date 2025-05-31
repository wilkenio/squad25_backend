package com.financeiro.api.service.impl;

import com.financeiro.api.repository.DashboardRepository;
import com.financeiro.api.service.DashboardService;
import com.financeiro.api.domain.Dashboard;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.dashboardDTO.DashboardRequestDTO;
import com.financeiro.api.dto.dashboardDTO.DashboardResponseDTO;
import com.financeiro.api.domain.enums.Status;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService{

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardResponseDTO create(DashboardRequestDTO dto) {
        Dashboard dashboard = new Dashboard();
        dashboard.setUrl(dto.url());
        dashboard.setName(dto.name());
        dashboard.setGraficType(dto.graficType());
        dashboard.setStatus(Status.SIM);
        dashboard.setCreatedAt(LocalDateTime.now());
        dashboard.setUpdatedAt(LocalDateTime.now());

        Dashboard saved = dashboardRepository.save(dashboard);
        return toDTO(saved);
    }

    @Override
    public DashboardResponseDTO update(UUID id, DashboardRequestDTO dto) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dashboard not found"));

        dashboard.setUrl(dto.url());
        dashboard.setName(dto.name());
        dashboard.setGraficType(dto.graficType());
        dashboard.setUpdatedAt(LocalDateTime.now());

        Dashboard updated = dashboardRepository.save(dashboard);
        return toDTO(updated);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Override
    public List<DashboardResponseDTO> findAll() {
        List<Status> statuses = List.of(Status.SIM, Status.NAO);
        User currentUser = getCurrentUser();
        return dashboardRepository.findAllByStatusInAndUser(statuses, currentUser)
               .stream()
               .map(this::toDTO)
               .collect(Collectors.toList());
    }

    @Override
    public DashboardResponseDTO findById(UUID id) {
        return dashboardRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Dashboard not found"));
    }

    @Override
    public void delete(UUID id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dashboard not found"));

        dashboard.setStatus(Status.EXC);
        dashboard.setUpdatedAt(LocalDateTime.now());
        dashboardRepository.save(dashboard);
    }

    private DashboardResponseDTO toDTO(Dashboard dashboard) {
        return new DashboardResponseDTO(
                dashboard.getId(),
                dashboard.getUrl(),
                dashboard.getName(),
                dashboard.getGraficType(),
                dashboard.getStatus());
    }
}
