package com.financeiro.api.controller;

import com.financeiro.api.dto.cardDTO.CardRequestDTO;
import com.financeiro.api.dto.cardDTO.CardResponseDTO;
import com.financeiro.api.service.impl.CardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class CardController {

    @Autowired
    private CardServiceImpl cardServiceImpl;

    @PostMapping
    public ResponseEntity<CardResponseDTO> create(@RequestBody CardRequestDTO dto) {
        return ResponseEntity.ok(cardServiceImpl.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> findAll() {
        return ResponseEntity.ok(cardServiceImpl.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardServiceImpl.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDTO> update(@PathVariable UUID id, @RequestBody CardRequestDTO dto) {
        return ResponseEntity.ok(cardServiceImpl.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}
