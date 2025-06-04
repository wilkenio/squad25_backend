package com.financeiro.api.service;

import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface SummariesService {
    Page<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro);
    void exportSummaryToCsv(TransactionAdvancedFilterDTO filtro, Writer writer) throws IOException;
    void exportSummaryToPdf(TransactionAdvancedFilterDTO filtro, OutputStream outputStream) throws IOException;
}