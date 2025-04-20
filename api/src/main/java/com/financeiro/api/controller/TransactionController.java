package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.service.impl.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionServiceImpl service;

    public TransactionController(TransactionServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> create(@RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<TransactionResponseDTO> updateState(
            @PathVariable UUID id,
            @RequestParam TransactionState state
    ) {
        return ResponseEntity.ok(service.updateState(id, state));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
