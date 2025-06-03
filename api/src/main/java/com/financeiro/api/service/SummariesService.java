package com.financeiro.api.service;

import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import org.springframework.data.domain.Page;
import java.util.List;

public interface SummariesService {
    Page<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro);
}