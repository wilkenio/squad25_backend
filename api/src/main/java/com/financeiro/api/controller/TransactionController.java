package com.financeiro.api.controller;

import com.financeiro.api.dto.accountDTO.AccountTransactionSummaryDTO;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.service.CsvImportService;
import com.financeiro.api.service.impl.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.TransactionState;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionServiceImpl service;
    private final CsvImportService csvImportService;

    public TransactionController(TransactionServiceImpl service, CsvImportService csvImportService) {
        this.service = service;
        this.csvImportService = csvImportService;
    }

    @PostMapping
    public ResponseEntity<List<TransactionResponseDTO>> create(@RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/import/csv")
    public ResponseEntity<String> importar(@RequestParam("file") MultipartFile file) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        csvImportService.importFromCsv(file, user);
        return ResponseEntity.ok("Importação concluída.");
    }

    @GetMapping
    public ResponseEntity<List<TransactionSimplifiedResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<TransactionResponseDTO> updateState(
            @PathVariable UUID id,
            @RequestParam TransactionState state
    ) {
        return ResponseEntity.ok(service.updateState(id, state));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionSimplifiedResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<AccountTransactionSummaryDTO>> getSummary(@RequestBody TransactionFilterDTO dto) {
        return ResponseEntity.ok(service.filtrarTransacoes(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
