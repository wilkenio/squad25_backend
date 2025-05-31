package com.financeiro.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.transactionDTO.TransactionSummaryDTO;

public record SummaryDTO(
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<TransactionSummaryDTO> transactions,
        TransactionOrder order) {
}