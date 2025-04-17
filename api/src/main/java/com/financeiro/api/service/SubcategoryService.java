package com.financeiro.api.service;

import com.financeiro.api.dto.subcategoryDTO.SubcategoryRequestDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryWithTransactionDTO;

import java.util.List;
import java.util.UUID;

public interface SubcategoryService {
    SubcategoryResponseDTO create(SubcategoryRequestDTO dto);
    List<SubcategoryResponseDTO> findAll();
    SubcategoryResponseDTO findById(UUID id);
    SubcategoryResponseDTO update(UUID id, SubcategoryRequestDTO dto);
    void delete(UUID id);
    List<SubcategoryWithTransactionDTO> listSubcategoriesByCategory(UUID categoryId);
}
