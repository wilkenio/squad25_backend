package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.categoryDTO.CategoryListDTO;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, 
                             UserRepository userRepository,
                             TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO dto, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuário nao encontrado"));

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
            .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

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
        Category category = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO findById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }

    @Override
    public List<CategoryResponseDTO> findByName(String name) {
            List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
            return categories.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> findByDateRange(LocalDateTime initialDate, LocalDateTime finalDate) {
        List<Category> categories = categoryRepository.findByCreatedAtBetween(initialDate, finalDate);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> findByStatus(Status status) {
        List<Category> categories = categoryRepository.findByStatus(status);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryListDTO> listCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> {
                    BigDecimal totalValue = transactionRepository.findByCategoryId(category.getId())
                            .stream()
                            .map(Transaction::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return new CategoryListDTO(
                            category.getIconClass(),
                            category.getName(),
                            totalValue
                    );
                })
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
