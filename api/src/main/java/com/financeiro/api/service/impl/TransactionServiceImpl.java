package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionServiceImpl(
            TransactionRepository repository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    public TransactionResponseDTO create(TransactionRequestDTO dto) {
        if (dto.type() == TransactionType.TRANSFER) {
            return createTransferTransaction(dto);
        }

        Account account = accountRepository.findById(dto.accountId()).orElseThrow();
        Category category = categoryRepository.findById(dto.categoryId()).orElseThrow();
        Subcategory subcategory = subcategoryRepository.findById(dto.subcategoryId()).orElseThrow();

        int installments = dto.installments() != null && dto.installments() > 0 ? dto.installments() : 1;
        List<Transaction> transactions = new ArrayList<>();
        LocalDateTime baseDate = dto.releaseDate() != null ? dto.releaseDate() : LocalDateTime.now();

        for (int i = 0; i < installments; i++) {
            Transaction tx = new Transaction();
            tx.setAccount(account);
            tx.setCategory(category);
            tx.setSubcategory(subcategory);
            tx.setName(dto.name() + (installments > 1 ? " (" + (i + 1) + "/" + installments + ")" : ""));
            tx.setType(dto.type());
            tx.setStatus(dto.status());
            tx.setValue(dto.value());
            tx.setDescription(dto.description());
            tx.setState(dto.state());
            tx.setAdditionalInformation(dto.additionalInformation());
            tx.setFrequency(dto.frequency());
            tx.setInstallments(installments);
            tx.setCreatedAt(LocalDateTime.now());
            tx.setUpdatedAt(LocalDateTime.now());

            LocalDateTime releaseDate = i == 0 ? baseDate : calculateNextDate(baseDate, dto.frequency(), i, dto.customDays());
            tx.setReleaseDate(releaseDate);

            if (dto.type() == TransactionType.EXPENSE || dto.type() == TransactionType.TRANSFER) {
                BigDecimal saldo = BigDecimal.valueOf(account.getOpeningBalance());
                BigDecimal totalDespesas = repository.findAll().stream()
                        .filter(t -> t.getAccount().getId().equals(account.getId()))
                        .map(Transaction::getValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (saldo.subtract(totalDespesas).compareTo(dto.value()) < 0) {
                    throw new IllegalArgumentException("Saldo insuficiente para realizar esta transação.");
                }
            }

            repository.save(tx);
            transactions.add(tx);
        }

        return toDTO(transactions.get(0));
    }

    private TransactionResponseDTO createTransferTransaction(TransactionRequestDTO dto) {
        UUID origemId = dto.accountId();
        UUID destinoId = dto.categoryId(); 

        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Conta de origem e destino não podem ser iguais.");
        }

        Account origem = accountRepository.findById(origemId).orElseThrow();
        Account destino = accountRepository.findById(destinoId).orElseThrow();

        int parcelas = dto.installments() != null && dto.installments() > 0 ? dto.installments() : 1;
        LocalDateTime baseDate = dto.releaseDate() != null ? dto.releaseDate() : LocalDateTime.now();

        for (int i = 0; i < parcelas; i++) {
            LocalDateTime date = i == 0 ? baseDate : calculateNextDate(baseDate, dto.frequency(), i, dto.customDays());

            Transaction debito = new Transaction();
            debito.setAccount(origem);
            debito.setName(dto.name() + " (Débito " + (i + 1) + "/" + parcelas + ")");
            debito.setType(TransactionType.TRANSFER);
            debito.setStatus(dto.status());
            debito.setValue(dto.value().negate());
            debito.setReleaseDate(date);
            debito.setState(dto.state());
            debito.setDescription("Transferência para: " + destino.getAccountName());
            debito.setFrequency(dto.frequency());
            debito.setInstallments(parcelas);
            debito.setCreatedAt(LocalDateTime.now());
            debito.setUpdatedAt(LocalDateTime.now());
            repository.save(debito);

            Transaction credito = new Transaction();
            credito.setAccount(destino);
            credito.setName(dto.name() + " (Crédito " + (i + 1) + "/" + parcelas + ")");
            credito.setType(TransactionType.INCOME);
            credito.setStatus(dto.status());
            credito.setValue(dto.value());
            credito.setReleaseDate(date);
            credito.setState(TransactionState.EFFECTIVE);
            credito.setDescription("Recebido de: " + origem.getAccountName());
            credito.setFrequency(dto.frequency());
            credito.setInstallments(parcelas);
            credito.setCreatedAt(LocalDateTime.now());
            credito.setUpdatedAt(LocalDateTime.now());
            repository.save(credito);
        }

        return new TransactionResponseDTO(
                UUID.randomUUID(), origemId, destinoId, null,
                dto.name(), dto.type(), dto.status(), baseDate, dto.value(),
                dto.description(), dto.state(), dto.additionalInformation(),
                dto.frequency(), dto.installments(), baseDate, baseDate
        );
    }

    private LocalDateTime calculateNextDate(LocalDateTime baseDate, Frequency frequency, int step, Integer customDays) {
        return switch (frequency) {
            case DAILY -> baseDate.plusDays(step);
            case WEEKLY -> baseDate.plusWeeks(step);
            case BIWEEKLY -> baseDate.plusWeeks(step * 2);
            case MONTHLY -> baseDate.plusMonths(step);
            case QUARTERLY -> baseDate.plusMonths(step * 3);
            case SEMIANNUAL -> baseDate.plusMonths(step * 6);
            case ANNUAL -> baseDate.plusYears(step);
            case CUSTOM -> baseDate.plusDays(step * (customDays != null ? customDays : 20));
            default -> baseDate;
        };
    }

    public List<TransactionResponseDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    public TransactionResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    public TransactionResponseDTO updateState(UUID id, TransactionState state) {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada"));
    
        transaction.setState(state);
        transaction.setUpdatedAt(LocalDateTime.now());
        repository.save(transaction);
    
        return toDTO(transaction);
    }    

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getSubcategory() != null ? transaction.getSubcategory().getId() : null,
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
