package com.financeiro.api.controller;

import com.financeiro.api.dto.subcategoryDTO.SubcategoryRequestDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;
import com.financeiro.api.service.SubcategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.financeiro.api.domain.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subcategory")
public class SubcategoryController {

    private final SubcategoryService service;

    public SubcategoryController(SubcategoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubcategoryResponseDTO> create(@RequestBody SubcategoryRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubcategoryResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<SubcategoryResponseDTO>> getByCategoryId(@PathVariable UUID categoryId) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(service.findByCategoryIdAndUser(categoryId, userId));
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubcategoryResponseDTO> update(@PathVariable UUID id, @RequestBody SubcategoryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
