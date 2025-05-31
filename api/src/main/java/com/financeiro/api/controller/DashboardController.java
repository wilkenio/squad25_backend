package com.financeiro.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.api.dto.dashboardDTO.DashboardRequestDTO;
import com.financeiro.api.dto.dashboardDTO.DashboardResponseDTO;
import com.financeiro.api.service.impl.DashboardServiceImpl;

@RestController
@RequestMapping("/dashboards")
public class DashboardController {
    private final DashboardServiceImpl dashboardService;

    public DashboardController(DashboardServiceImpl dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping
    public ResponseEntity<DashboardResponseDTO> create(DashboardRequestDTO dto){
        return ResponseEntity.ok(dashboardService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<DashboardResponseDTO>> findAll(){
        return ResponseEntity.ok(dashboardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DashboardResponseDTO> findById(UUID id){
        return ResponseEntity.ok(dashboardService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DashboardResponseDTO> update(UUID id, DashboardRequestDTO dto){
        return ResponseEntity.ok(dashboardService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UUID id){
        dashboardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
