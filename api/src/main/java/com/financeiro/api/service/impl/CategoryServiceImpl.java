package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryListDTO; // Importação ajustada
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.financeiro.api.domain.enums.Status;

import java.math.BigDecimal;
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
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO dto, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.name());
        category.setType(dto.type());
        category.setIconClass(dto.iconClass());
        category.setColor(dto.color());
        category.setAdditionalInfo(dto.additionalInfo());
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
        category.setColor(dto.color());
        category.setAdditionalInfo(dto.additionalInfo());
        category.setStandardRecommendation(dto.standardRecommendation());
        category.setStatus(dto.status());
        category.setUpdatedAt(LocalDateTime.now());

        Category updated = categoryRepository.save(category);
        return toDTO(updated);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        
        category.setStatus(Status.EXC); 
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);    
    }

    @Override
    public CategoryResponseDTO findById(UUID id) {
        return categoryRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    public List<CategoryResponseDTO> findAll(UUID userId) {
        List<Status> statuses = List.of(Status.SIM, Status.NAO);
        return categoryRepository.findAllByUserIdAndStatusIn(userId, statuses)
            .stream()
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
    public List<CategoryResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Category> categories = categoryRepository.findByCreatedAtBetween(startDate, endDate);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> findByName(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryListDTO> listCategories(UUID userId) {
        List<Status> statuses = List.of(Status.SIM, Status.NAO);
    
        List<Category> categories = categoryRepository.findAllByUserIdAndStatusIn(userId, statuses);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    
        return categories.stream()
                .map(category -> {
                    BigDecimal totalValue = transactionRepository.findByCategoryId(category.getId())
                            .stream()
                            .filter(transaction -> {
                                LocalDateTime transactionDate = transaction.getCreatedAt();
                                return !transactionDate.isBefore(startOfMonth) && !transactionDate.isAfter(endOfMonth);
                            })
                            .map(Transaction::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
                    return new CategoryListDTO(
                            category.getId(),
                            category.getName(),
                            category.getType(),
                            category.getIconClass(),
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
                category.getColor(),
                category.getAdditionalInfo(),
                category.isStandardRecommendation(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

}
