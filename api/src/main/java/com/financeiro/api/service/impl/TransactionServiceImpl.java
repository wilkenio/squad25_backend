package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.infra.exceptions.TransactionNotFoundException;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionServiceImpl(TransactionRepository repository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository) {
        this.transactionRepository = repository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Override
    public TransactionResponseDTO create(TransactionRequestDTO dto) {

        Account account = accountRepository.findById(dto.accounId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
        subcategory = subcategoryRepository.findById(dto.subcategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));
        }


        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setSubcategory(subcategory);
        transaction.setName(dto.name());
        transaction.setType(dto.type());
        transaction.setStatus(dto.status());
        transaction.setReleaseDate(dto.releaseDate());
        transaction.setValue(dto.value());
        transaction.setDescription(dto.description());
        transaction.setState(dto.state());
        transaction.setAdditionalInformation(dto.additionalInformation());
        transaction.setFrequency(dto.frequency());
        transaction.setInstallments(dto.installments());

        LocalDateTime now = java.time.LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        // Salva a transação e retorna o DTO
        Transaction savedTransaction = transactionRepository.save(transaction);
        return toDTO(savedTransaction);
    }

    @Override
    public List<TransactionSimplifiedResponseDTO> findAll() {
        return transactionRepository.findAll().stream()
                .map(transaction -> new TransactionSimplifiedResponseDTO(
                        transaction.getName(),
                        transaction.getType(),
                        transaction.getAccount().getAccountName(),
                        transaction.getReleaseDate(),
                        transaction.getFrequency(),
                        transaction.getValue()))
                .toList();
    }

    @Override
    public TransactionSimplifiedResponseDTO findById(UUID id) {
        return transactionRepository.findById(id)
                .map(transaction -> new TransactionSimplifiedResponseDTO(
                        transaction.getName(),
                        transaction.getType(),
                        transaction.getAccount().getAccountName(),
                        transaction.getReleaseDate(),
                        transaction.getFrequency(),
                        transaction.getValue()))
                .orElseThrow(() -> new TransactionNotFoundException());
    }

    @Override
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }

    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getAccount(),
                transaction.getCategory(),
                transaction.getSubcategory(),
                transaction.getId(),
                transaction.getName(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getReleaseDate(),
                transaction.getValue(),
                transaction.getDescription(),
                transaction.getState(),
                transaction.getAdditionalInformation(),
                transaction.getFrequency(),
                transaction.getInstallments(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }
}
