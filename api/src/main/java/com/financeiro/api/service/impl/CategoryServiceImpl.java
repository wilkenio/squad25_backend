package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO dto, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setType(dto.type());
        category.setIconClass(dto.iconClass());
        category.setStandardRecommendation(dto.standardRecommendation());
        category.setStatus(dto.status());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
    
        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }
    
    @Override
    public CategoryResponseDTO update(UUID id, CategoryRequestDTO dto, UUID userId) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
        category.setUser(user);
        category.setName(dto.name());
        category.setType(dto.type());
        category.setIconClass(dto.iconClass());
        category.setStandardRecommendation(dto.standardRecommendation());
        category.setStatus(dto.status());
        category.setUpdatedAt(LocalDateTime.now());
    
        Category updated = categoryRepository.save(category);
        return toDTO(updated);
    }
    

    @Override
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponseDTO findById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CategoryResponseDTO toDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getUser().getId(),
                category.getName(),
                category.getType(),
                category.getIconClass(),
                category.isStandardRecommendation(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
