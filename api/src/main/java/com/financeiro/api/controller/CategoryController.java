package com.financeiro.api.controller;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    //Diferentes getters com uma única rota
    @GetMapping("/get/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping("get/{name}")
    public ResponseEntity<List<CategoryResponseDTO>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.findByName(name));
    }

    @GetMapping("get/{finalDate}")
    public ResponseEntity<List<CategoryResponseDTO>> getByDateRange(LocalDateTime initialDate) {
        LocalDateTime finalDate = LocalDateTime.now();//comparar a data atual com a data de busca do usuário
        return ResponseEntity.ok(categoryService.findByDateRange(initialDate, finalDate));
    }

    @GetMapping("get/{status}")
    public ResponseEntity<List<CategoryResponseDTO>> getByStatus(Status status) {
        return ResponseEntity.ok(categoryService.findByStatus(status));
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
