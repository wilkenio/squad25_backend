package com.financeiro.api.service;

import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO dto, UUID userId);

    CategoryResponseDTO update(UUID id, CategoryRequestDTO dto, UUID userId);

    void delete(UUID id, UUID userId);

    CategoryResponseDTO findById(UUID id);

    List<CategoryResponseDTO> findAll(UUID userId);

    CategoryResponseDTO findByName(String name);
}
