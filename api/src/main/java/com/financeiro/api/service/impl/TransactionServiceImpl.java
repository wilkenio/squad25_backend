package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionServiceImpl(TransactionRepository repository,
                                  AccountRepository accountRepository,
                                  CategoryRepository categoryRepository,
                                  SubcategoryRepository subcategoryRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    public TransactionResponseDTO create(TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();

        transaction.setAccount(accountRepository.findById(dto.accountId()).orElseThrow());
        transaction.setCategory(categoryRepository.findById(dto.categoryId()).orElseThrow());
        transaction.setSubcategory(subcategoryRepository.findById(dto.subcategoryId()).orElseThrow());

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
        transaction.setCreatedAt(java.time.LocalDateTime.now());
        transaction.setUpdatedAt(java.time.LocalDateTime.now());

        repository.save(transaction);

        return toDTO(transaction);
    }

    public List<TransactionResponseDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public TransactionResponseDTO findById(UUID id) {
        return repository.findById(id).map(this::toDTO).orElseThrow(
                () -> new EntityNotFoundException("Transaction not found")
        );
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getCategory().getId(),
                transaction.getSubcategory().getId(),
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
                transaction.getUpdatedAt()
        );
    }
}
