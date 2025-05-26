package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.AccountTransactionSummaryDTO;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.dto.transferDTO.TransferRequestDTO;
import com.financeiro.api.service.CsvImportService;
import com.financeiro.api.service.TransferService;
import com.financeiro.api.service.impl.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionServiceImpl service;
    private final CsvImportService csvImportService;
    private final TransferService transferService;

    public TransactionController(
            TransactionServiceImpl service,
            CsvImportService csvImportService,
            TransferService transferService) {
        this.service = service;
        this.csvImportService = csvImportService;
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<List<TransactionResponseDTO>> create(@RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponseDTO>> transfer(@RequestBody TransferRequestDTO dto) {
        return ResponseEntity.ok(transferService.transfer(dto));
    }

    @PostMapping("/import/csv")
    public ResponseEntity<String> importar(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "accountId", required = false) UUID accountId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        csvImportService.importFromCsv(file, user, accountId);
        return ResponseEntity.ok("Importação concluída com sucesso.");
    }

    @PostMapping("/filter")
    public ResponseEntity<List<TransactionResponseDTO>> filtrarAvancado(@RequestBody TransactionAdvancedFilterDTO dto) {
        return ResponseEntity.ok(service.filtrarAvancado(dto));
    }

    @GetMapping
    public ResponseEntity<List<TransactionSimplifiedResponseDTO>> getAll(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.findAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionSimplifiedResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponseDTO>> filtrarAvancado(
            @RequestParam(required = false) List<UUID> contaIds,
            @RequestParam(required = false) List<UUID> categoriaIds,
            @RequestParam(required = false) TransactionType tipo,
            @RequestParam(required = false) TransactionState estado,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim
    ) {
        TransactionAdvancedFilterDTO dto = new TransactionAdvancedFilterDTO(
                contaIds, categoriaIds, tipo, estado, dataInicio, dataFim
        );
        return ResponseEntity.ok(service.filtrarAvancado(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(@PathVariable UUID id,
            @RequestBody RecurringUpdateRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<TransactionResponseDTO> updateState(@PathVariable UUID id,
            @RequestParam TransactionState state) {
        return ResponseEntity.ok(service.updateState(id, state));
    }

    @DeleteMapping("/recurring/{groupId}")
    public ResponseEntity<Void> cancelarTransacoesRecorrentes(@PathVariable UUID groupId) {
        service.cancelarRecorrencia(groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
