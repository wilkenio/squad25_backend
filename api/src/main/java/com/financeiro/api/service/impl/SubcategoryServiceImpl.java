package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryRequestDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryWithTransactionDTO;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.SubcategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubcategoryServiceImpl implements SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public SubcategoryServiceImpl(SubcategoryRepository subcategoryRepository, 
                                CategoryRepository categoryRepository,
                                TransactionRepository transactionRepository) {
        this.subcategoryRepository = subcategoryRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public SubcategoryResponseDTO create(SubcategoryRequestDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Subcategory subcategory = new Subcategory();
        subcategory.setName(dto.name());
        subcategory.setStandardRecommendation(dto.standardRecommendation());
        subcategory.setCategory(category);
        subcategory.setIconClass(dto.iconClass());
        subcategory.setColor(dto.color());
        subcategory.setAdditionalInfo(dto.additionalInfo());
        subcategory.setStatus(Status.SIM);
        subcategory.setCreatedAt(LocalDateTime.now());
        subcategory.setUpdatedAt(LocalDateTime.now());

        subcategoryRepository.save(subcategory);
        return toDTO(subcategory);
    }

    @Override
    public List<SubcategoryResponseDTO> findAll() {
        List<Status> statuses = List.of(Status.SIM, Status.NAO);
        return subcategoryRepository.findAllByStatusIn(statuses).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubcategoryResponseDTO findById(UUID id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
        .filter(s -> s.getStatus() != Status.EXC)
        .orElseThrow(
                () -> new EntityNotFoundException("Subcategory not found")
        );
        return toDTO(subcategory);
    }

    @Override
    public SubcategoryResponseDTO update(UUID id, SubcategoryRequestDTO dto) {
        Subcategory subcategory = subcategoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Subcategory not found")
        );

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        subcategory.setName(dto.name());
        subcategory.setStandardRecommendation(dto.standardRecommendation());
        subcategory.setCategory(category);
        subcategory.setIconClass(dto.iconClass());
        subcategory.setColor(dto.color());
        subcategory.setAdditionalInfo(dto.additionalInfo());
        subcategory.setUpdatedAt(LocalDateTime.now());

        subcategoryRepository.save(subcategory);
        return toDTO(subcategory);
    }

    @Override
    public void delete(UUID id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Subcategory not found")
                );
        subcategory.setStatus(Status.EXC);
        subcategory.setUpdatedAt(LocalDateTime.now());
        subcategoryRepository.save(subcategory);
    }

    @Override
    public List<SubcategoryWithTransactionDTO> listSubcategoriesByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        return subcategoryRepository.findAll().stream()
                .filter(subcategory -> subcategory.getCategory().getId().equals(categoryId))
                .map(subcategory -> {
                    BigDecimal totalValue = transactionRepository.findAll().stream()
                            .filter(transaction -> 
                                transaction.getSubcategory() != null &&
                                transaction.getSubcategory().getId().equals(subcategory.getId())
                            )
                            .map(Transaction::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new SubcategoryWithTransactionDTO(
                            subcategory.getName(),
                            subcategory.getIconClass(),
                            totalValue
                    );
                })
                .collect(Collectors.toList());
    }

    private SubcategoryResponseDTO toDTO(Subcategory subcategory) {
        return new SubcategoryResponseDTO(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getStandardRecommendation(),
                subcategory.getCategory().getId(),
                subcategory.getIconClass(),
                subcategory.getStatus(),
                subcategory.getColor(),
                subcategory.getAdditionalInfo(),
                subcategory.getCreatedAt(),
                subcategory.getUpdatedAt()
        );
    }
}
