package com.financeiro.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.SummaryDTO;
import com.financeiro.api.service.impl.SummaryServiceImpl;

@RestController
@RequestMapping("/relatorio")
public class SummaryController {
    
    private final SummaryServiceImpl summaryServiceImpl;

    public SummaryController(SummaryServiceImpl summaryServiceImpl) {
        this.summaryServiceImpl = summaryServiceImpl;
    }

    @GetMapping("/summary")
    public ResponseEntity<List<SummaryDTO>> findSummary(
            @RequestParam(required = true) List<UUID> accountsId,
            @RequestParam(required = true) List<UUID> categoriesId,
            @RequestParam(required = true) LocalDateTime startDate,
            @RequestParam(required = true) LocalDateTime endDate,
            @RequestParam(required = true) TransactionOrder order) {
        return ResponseEntity.ok(summaryServiceImpl.findSummary(accountsId, categoriesId, order, startDate, endDate));
    }
}
