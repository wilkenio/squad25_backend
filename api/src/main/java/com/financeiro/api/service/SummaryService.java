package com.financeiro.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.SummaryDTO;

public interface SummaryService {
    List<SummaryDTO> findSummary(List<UUID> accountsId, List<UUID> categoriesId, TransactionOrder order, LocalDateTime startDate, LocalDateTime endDate);
}
