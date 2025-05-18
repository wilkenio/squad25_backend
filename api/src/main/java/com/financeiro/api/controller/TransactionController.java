package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.TransactionState;
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
            TransferService transferService
    ) {
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

    @GetMapping
    public ResponseEntity<List<TransactionSimplifiedResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionSimplifiedResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> update(@PathVariable UUID id, @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<TransactionResponseDTO> updateState(@PathVariable UUID id, @RequestParam TransactionState state) {
        return ResponseEntity.ok(service.updateState(id, state));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
