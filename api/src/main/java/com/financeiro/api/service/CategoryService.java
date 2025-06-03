package com.financeiro.api.service;

import com.financeiro.api.dto.categoryDTO.CategoryListDTO;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseByIdDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO dto, UUID userId);

    CategoryResponseDTO update(UUID id, CategoryRequestDTO dto, UUID userId);

    void delete(UUID id, UUID userId);

    CategoryResponseByIdDTO findById(UUID id);

    List<CategoryResponseDTO> findAll();

    List<CategoryResponseDTO> findByName(String name);

    List<CategoryResponseDTO> findByDateRange(LocalDateTime initialDate, LocalDateTime finalDate);

    List<CategoryResponseDTO> findByStatus(Status status);

    List<CategoryListDTO> listCategories();
}
