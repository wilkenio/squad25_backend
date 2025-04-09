package com.financeiro.api.controller;

import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.service.impl.CategoryServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    private final CategoryServiceImpl categoryService;

    public CategoryController(CategoryServiceImpl categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@RequestBody CategoryRequestDTO dto) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(categoryService.create(dto, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable UUID id, @RequestBody CategoryRequestDTO dto) {
        UUID userId = getCurrentUserId();
        return ResponseEntity.ok(categoryService.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    @GetMapping("name/{name}")
    public ResponseEntity<List<CategoryResponseDTO>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.findByName(name));
    }

    @GetMapping("date/{finalDate}")
    public ResponseEntity<List<CategoryResponseDTO>> getByDateRange(LocalDateTime initialDate) {
        LocalDateTime finalDate = LocalDateTime.now();//comparar a data atual com a data de busca do usuário
        return ResponseEntity.ok(categoryService.findByDateRange(initialDate, finalDate));
    }

    @GetMapping("status/{status}")
    public ResponseEntity<List<CategoryResponseDTO>> getByStatus(Status status) {
        return ResponseEntity.ok(categoryService.findByStatus(status));
    }
}
