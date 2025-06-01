package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.service.SummariesService;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<DashboardItemDTO>> getSummaryViaPost(@RequestBody TransactionAdvancedFilterDTO dto) { 
        Page<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }

    @GetMapping("/summaries")
    public ResponseEntity<Page<DashboardItemDTO>> getSummaryViaGet(
            //Filtro Geral
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

            //Filtros Adicionais
            @RequestParam(required = false) Boolean incluirTodasCategoriasReceita,
            @RequestParam(required = false) List<UUID> idsCategoriasReceitaEspecificas, 
            @RequestParam(required = false) Boolean incluirTodasCategoriasDespesa,
            @RequestParam(required = false) List<UUID> idsCategoriasDespesaEspecificas,
            @RequestParam(required = false) Boolean incluirFreqNaoRecorrente,
            @RequestParam(required = false) Boolean incluirFreqFixaMensal,
            @RequestParam(required = false) Boolean incluirFreqRepetida,

            //Apresentação de dados
            @RequestParam(required = false) TransactionOrder ordenacao,
            @RequestParam(required = false) TipoDado tipoDado,
            @RequestParam(required = false) TipoApresentacaoDados apresentacao,
            @RequestParam(required = false) Integer limite,
            
            // Parâmetros de Paginação
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
            ) {

        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                dataInicio, 
                dataFim,
                contaIds, 
                mostrarApenasSaldo,
                incluirSaldoPrevisto,
                incluirReceitas,
                incluirReceitasEfetivadas, 
                incluirReceitasPrevistas,
                incluirDespesas,
                incluirDespesasEfetivadas, 
                incluirDespesasPrevistas,
                incluirTransferencias,
                incluirTransferenciasEfetivadas, 
                incluirTransferenciasPrevistas,

                incluirTodasCategoriasReceita, 
                idsCategoriasReceitaEspecificas, 
                incluirTodasCategoriasDespesa,
                idsCategoriasDespesaEspecificas,
                incluirFreqNaoRecorrente,
                incluirFreqFixaMensal,
                incluirFreqRepetida, 
                
                ordenacao, 
                tipoDado,
                apresentacao, 
                limite,
                
                pageNumber, 
                pageSize
        );
        Page<DashboardItemDTO> summaryResults = summaryService.generateSummary(dto);
        return ResponseEntity.ok(summaryResults);
    }
}