package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.service.SummariesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<List<DashboardItemDTO>> getSummaryViaPost(@RequestBody TransactionAdvancedFilterDTO dto) { // Tipo de retorno atualizado
        List<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<DashboardItemDTO>> getSummaryViaGet( // Tipo de retorno atualizado
            @RequestParam(required = false) List<UUID> contaIds,
            @RequestParam(required = false) List<UUID> categoriaIds,
            @RequestParam(required = false) CategoryType categoriaTipo,
            @RequestParam(required = false) TransactionType transacaoTipo,
            @RequestParam(required = false) TransactionState estado,
            @RequestParam(required = false) Frequency frequencia,
            @RequestParam(required = false) DataReferencia dataReferencia,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(required = false) TransactionOrder ordenacao,
            @RequestParam(required = false) TipoApresentacaoDados apresentacao,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) TipoDado tipoDado,
            @RequestParam(required = false) Boolean incluirSaldoPrevisto,
            @RequestParam(required = false) Boolean mostrarApenasSaldo
    ) {
        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                contaIds, categoriaIds, categoriaTipo, transacaoTipo, estado, frequencia,
                dataReferencia, dataInicio, dataFim, ordenacao, apresentacao, limite,
                tipoDado, incluirSaldoPrevisto, mostrarApenasSaldo
        );
        List<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }
}