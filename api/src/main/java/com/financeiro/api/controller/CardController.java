package com.financeiro.api.controller;

import com.financeiro.api.dto.cardDTO.CardRequestDTO;
import com.financeiro.api.dto.cardDTO.CardResponseDTO;
import com.financeiro.api.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponseDTO> create(@RequestBody CardRequestDTO dto) {
        return ResponseEntity.ok(cardService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> findAll() {
        return ResponseEntity.ok(cardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDTO> update(@PathVariable UUID id, @RequestBody CardRequestDTO dto) {
        return ResponseEntity.ok(cardService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
