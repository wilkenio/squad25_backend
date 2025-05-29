package com.financeiro.api.service;

import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import java.util.List;

public interface SummariesService {
    List<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro); // Tipo de retorno atualizado
}