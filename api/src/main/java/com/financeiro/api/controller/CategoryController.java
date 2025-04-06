package com.financeiro.api.controller;

import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/post")
    public ResponseEntity<CategoryResponseDTO> create(@RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.create(dto));
    }

    @GetMapping("/get")
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable UUID id, @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
