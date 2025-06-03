package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.categoryDTO.CategoryRequestDTO;
import com.financeiro.api.dto.categoryDTO.CategoryListDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseDTO;
import com.financeiro.api.dto.categoryDTO.CategoryResponseByIdDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.financeiro.api.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        List<Status> statuses = List.of(Status.SIM, Status.NAO);
        User currentUser = getCurrentUser();
        return categoryRepository.findAllByStatusInAndUser(statuses, currentUser)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseByIdDTO findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        List<Subcategory> subcategories = subcategoryRepository.findByCategoryIdAndCategoryUserIdAndStatusIn(
                category.getId(),
                category.getUser().getId(),
                List.of(Status.SIM, Status.NAO));

        List<SubcategoryResponseDTO> subcategoryDTOs = subcategories.stream()
                .map(sub -> new SubcategoryResponseDTO(
                        sub.getId(),
                        sub.getName(),
                        sub.getStandardRecommendation(),
                        sub.getCategory().getId(),
                        sub.getIconClass(),
                        sub.getStatus(),
                        sub.getColor(),
                        sub.getAdditionalInfo(),
                        sub.getCreatedAt(),
                        sub.getUpdatedAt()))
                .collect(Collectors.toList());

        return new CategoryResponseByIdDTO(
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
                category.getUpdatedAt(),
                subcategoryDTOs);
    }

    @Override
    public List<CategoryResponseDTO> findByStatus(Status status) {
        return categoryRepository.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return categoryRepository.findByCreatedAtBetween(startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> findByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryListDTO> listCategories() {
        User currentUser = getCurrentUser();
        List<Category> categories = categoryRepository.findAllByUser(currentUser);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate()
                .lengthOfMonth()).withHour(23).withMinute(59)
                .withSecond(59).withNano(999999999);

        return categories.stream()
                .map(category -> {
                    Double totalValue = transactionRepository.findByCategoryId(category.getId())
                            .stream()
                            .filter(transaction -> {
                                LocalDateTime transactionDate = transaction.getCreatedAt();
                                return !transactionDate.isBefore(startOfMonth)
                                        && !transactionDate.isAfter(endOfMonth);
                            })
                            .map(Transaction::getValue)
                            .reduce(0.0, Double::sum);

                    return new CategoryListDTO(
                            category.getId(),
                            category.getName(),
                            category.getType(),
                            category.getIconClass(),
                            category.getColor(),
                            totalValue);
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
                category.getUpdatedAt());
    }

}
