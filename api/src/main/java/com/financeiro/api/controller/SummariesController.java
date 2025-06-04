package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO;
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.service.SummariesService;
import jakarta.servlet.http.HttpServletResponse; 
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/relatorios")
public class SummariesController {

    private final SummariesService summaryService;

    public SummariesController(SummariesService summaryService) {
        this.summaryService = summaryService;
    }

    @PostMapping("/summaries")
    public ResponseEntity<Page<DashboardItemDTO>> getSummaryViaPost(@RequestBody TransactionAdvancedFilterDTO dto) {
        Page<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }

    @GetMapping("/summaries")
    public ResponseEntity<Page<DashboardItemDTO>> getSummaryViaGet(
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(required = false) List<UUID> contaIds,
            @RequestParam(required = false) Boolean mostrarApenasSaldo,
            @RequestParam(required = false) Boolean incluirSaldoPrevisto,
            @RequestParam(required = false) Boolean incluirReceitas,
            @RequestParam(required = false) Boolean incluirReceitasEfetivadas,
            @RequestParam(required = false) Boolean incluirReceitasPrevistas,
            @RequestParam(required = false) Boolean incluirDespesas,
            @RequestParam(required = false) Boolean incluirDespesasEfetivadas,
            @RequestParam(required = false) Boolean incluirDespesasPrevistas,
            @RequestParam(required = false) Boolean incluirTransferencias,
            @RequestParam(required = false) Boolean incluirTransferenciasEfetivadas,
            @RequestParam(required = false) Boolean incluirTransferenciasPrevistas,
            @RequestParam(required = false) Boolean incluirTodasCategoriasReceita,
            @RequestParam(required = false) List<UUID> idsCategoriasReceita,
            @RequestParam(required = false) Boolean incluirTodasCategoriasDespesa,
            @RequestParam(required = false) List<UUID> idsCategoriasDespesa,
            @RequestParam(required = false) Boolean incluirFreqNaoRecorrente,
            @RequestParam(required = false) Boolean incluirFreqFixaMensal,
            @RequestParam(required = false) Boolean incluirFreqRepetida,
            @RequestParam(required = false) TransactionOrder ordenacao,
            @RequestParam(required = false) TipoDado tipoDado,
            @RequestParam(required = false) TipoApresentacaoDados apresentacao,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
            ) {

        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                dataInicio, dataFim, contaIds, mostrarApenasSaldo, incluirSaldoPrevisto,
                incluirReceitas, incluirReceitasEfetivadas, incluirReceitasPrevistas,
                incluirDespesas, incluirDespesasEfetivadas, incluirDespesasPrevistas,
                incluirTransferencias, incluirTransferenciasEfetivadas, incluirTransferenciasPrevistas,
                incluirTodasCategoriasReceita, idsCategoriasReceita,
                incluirTodasCategoriasDespesa, idsCategoriasDespesa,
                incluirFreqNaoRecorrente, incluirFreqFixaMensal, incluirFreqRepetida,
                ordenacao, tipoDado, apresentacao, limite,
                pageNumber, pageSize
        );
        Page<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }

    @GetMapping("/summaries/export/csv")
    public void exportSummariesToCsv(
            HttpServletResponse response, 
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(required = false) List<UUID> contaIds,
            @RequestParam(required = false) Boolean mostrarApenasSaldo,
            @RequestParam(required = false) Boolean incluirSaldoPrevisto,
            @RequestParam(required = false) Boolean incluirReceitas,
            @RequestParam(required = false) Boolean incluirReceitasEfetivadas,
            @RequestParam(required = false) Boolean incluirReceitasPrevistas,
            @RequestParam(required = false) Boolean incluirDespesas,
            @RequestParam(required = false) Boolean incluirDespesasEfetivadas,
            @RequestParam(required = false) Boolean incluirDespesasPrevistas,
            @RequestParam(required = false) Boolean incluirTransferencias,
            @RequestParam(required = false) Boolean incluirTransferenciasEfetivadas,
            @RequestParam(required = false) Boolean incluirTransferenciasPrevistas,
            @RequestParam(required = false) Boolean incluirTodasCategoriasReceita,
            @RequestParam(required = false) List<UUID> idsCategoriasReceita,
            @RequestParam(required = false) Boolean incluirTodasCategoriasDespesa,
            @RequestParam(required = false) List<UUID> idsCategoriasDespesa,
            @RequestParam(required = false) Boolean incluirFreqNaoRecorrente,
            @RequestParam(required = false) Boolean incluirFreqFixaMensal,
            @RequestParam(required = false) Boolean incluirFreqRepetida,
            @RequestParam(required = false) TransactionOrder ordenacao,
            @RequestParam(required = false) TipoDado tipoDado,
            @RequestParam(required = false) TipoApresentacaoDados apresentacao,
            @RequestParam(required = false) Integer limite
        ) throws IOException {

        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                dataInicio, dataFim, contaIds, mostrarApenasSaldo, incluirSaldoPrevisto,
                incluirReceitas, incluirReceitasEfetivadas, incluirReceitasPrevistas,
                incluirDespesas, incluirDespesasEfetivadas, incluirDespesasPrevistas,
                incluirTransferencias, incluirTransferenciasEfetivadas, incluirTransferenciasPrevistas,
                incluirTodasCategoriasReceita, idsCategoriasReceita,
                incluirTodasCategoriasDespesa, idsCategoriasDespesa,
                incluirFreqNaoRecorrente, incluirFreqFixaMensal, incluirFreqRepetida,
                ordenacao, tipoDado, apresentacao, limite,
                null, null 
        );

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "relatorio_financeiro_" + currentDateTime + ".csv";

        response.setContentType("text/csv; charset=UTF-8"); 
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        summaryService.exportSummaryToCsv(dto, response.getWriter());
    }

        @GetMapping("/summaries/export/pdf")
    public void exportSummariesToPdf(
            HttpServletResponse response, 
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(required = false) List<UUID> contaIds,
            @RequestParam(required = false) Boolean mostrarApenasSaldo,
            @RequestParam(required = false) Boolean incluirSaldoPrevisto,
            @RequestParam(required = false) Boolean incluirReceitas,
            @RequestParam(required = false) Boolean incluirReceitasEfetivadas,
            @RequestParam(required = false) Boolean incluirReceitasPrevistas,
            @RequestParam(required = false) Boolean incluirDespesas,
            @RequestParam(required = false) Boolean incluirDespesasEfetivadas,
            @RequestParam(required = false) Boolean incluirDespesasPrevistas,
            @RequestParam(required = false) Boolean incluirTransferencias,
            @RequestParam(required = false) Boolean incluirTransferenciasEfetivadas,
            @RequestParam(required = false) Boolean incluirTransferenciasPrevistas,
            @RequestParam(required = false) Boolean incluirTodasCategoriasReceita,
            @RequestParam(required = false) List<UUID> idsCategoriasReceita,
            @RequestParam(required = false) Boolean incluirTodasCategoriasDespesa,
            @RequestParam(required = false) List<UUID> idsCategoriasDespesa,
            @RequestParam(required = false) Boolean incluirFreqNaoRecorrente,
            @RequestParam(required = false) Boolean incluirFreqFixaMensal,
            @RequestParam(required = false) Boolean incluirFreqRepetida,
            @RequestParam(required = false) TransactionOrder ordenacao,
            @RequestParam(required = false) TipoDado tipoDado,
            @RequestParam(required = false) TipoApresentacaoDados apresentacao,
            @RequestParam(required = false) Integer limite
        ) throws IOException {

        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                dataInicio, dataFim, contaIds, mostrarApenasSaldo, incluirSaldoPrevisto,
                incluirReceitas, incluirReceitasEfetivadas, incluirReceitasPrevistas,
                incluirDespesas, incluirDespesasEfetivadas, incluirDespesasPrevistas,
                incluirTransferencias, incluirTransferenciasEfetivadas, incluirTransferenciasPrevistas,
                incluirTodasCategoriasReceita, idsCategoriasReceita,
                incluirTodasCategoriasDespesa, idsCategoriasDespesa,
                incluirFreqNaoRecorrente, incluirFreqFixaMensal, incluirFreqRepetida,
                ordenacao, tipoDado, apresentacao, limite,
                null, null 
        );

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "relatorio_financeiro_" + currentDateTime + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        summaryService.exportSummaryToPdf(dto, response.getOutputStream());
    }
}