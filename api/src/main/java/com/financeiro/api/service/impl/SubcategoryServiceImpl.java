package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryRequestDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryResponseDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategoryWithTransactionDTO;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.SubcategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubcategoryServiceImpl implements SubcategoryService {

        private final SubcategoryRepository subcategoryRepository;
        private final CategoryRepository categoryRepository;
        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;

        public SubcategoryServiceImpl(SubcategoryRepository subcategoryRepository,
                        CategoryRepository categoryRepository,
                        TransactionRepository transactionRepository,
                        UserRepository userRepository) {
                this.subcategoryRepository = subcategoryRepository;
                this.categoryRepository = categoryRepository;
                this.transactionRepository = transactionRepository;
                this.userRepository = userRepository;
        }

        private User getCurrentUser() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                return (User) auth.getPrincipal();
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
                User currentUser = getCurrentUser();
                return subcategoryRepository.findAllByStatusInAndCategoryUser(statuses, currentUser).stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public SubcategoryResponseDTO findById(UUID id) {
                Subcategory subcategory = subcategoryRepository.findById(id)
                                .filter(s -> s.getStatus() != Status.EXC)
                                .orElseThrow(
                                                () -> new EntityNotFoundException("Subcategory not found"));
                return toDTO(subcategory);
        }

        @Override
        public SubcategoryResponseDTO update(UUID id, SubcategoryRequestDTO dto) {
                Subcategory subcategory = subcategoryRepository.findById(id).orElseThrow(
                                () -> new EntityNotFoundException("Subcategoria não encontrada"));

                Category category = categoryRepository.findById(dto.categoryId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

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
                                                () -> new EntityNotFoundException("Subcategoria não encontrada"));
                subcategory.setStatus(Status.EXC);
                subcategory.setUpdatedAt(LocalDateTime.now());
                subcategoryRepository.save(subcategory);
        }

        @Override
        public List<SubcategoryWithTransactionDTO> findByCategoryIdAndUserId(UUID categoryId, UUID userId) {
            
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
            User currentUser = getCurrentUser();
            if (!category.getUser().getId().equals(currentUser.getId())) {
                throw new EntityNotFoundException("Categoria não encontrada para este usuário");
            }
    
            List<Status> statuses = List.of(Status.SIM, Status.NAO);
            List<Subcategory> subs = subcategoryRepository
                    .findByCategoryIdAndCategoryUserIdAndStatusIn(categoryId, currentUser.getId(), statuses);
    
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    
            return subs.stream()
                    .map(sub -> {
                        double total = transactionRepository.findByCategoryId(categoryId).stream()
                                .filter(tx -> tx.getSubcategory() != null
                                        && tx.getSubcategory().getId().equals(sub.getId())
                                        && !tx.getCreatedAt().isBefore(start)
                                        && !tx.getCreatedAt().isAfter(end))
                                .mapToDouble(Transaction::getValue)
                                .sum();
                        return new SubcategoryWithTransactionDTO(
                                sub.getId(),
                                sub.getName(),
                                sub.getIconClass(),
                                sub.getColor(),
                                sub.getCategory().getType(),
                                total
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
                                subcategory.getUpdatedAt());
        }
}
