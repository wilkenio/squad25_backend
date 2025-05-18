package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.accountDTO.AccountTransactionSummaryDTO;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.infra.exceptions.TransactionNotFoundException;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<TransactionResponseDTO> create(TransactionRequestDTO dto) {
        if (dto.frequency() == Frequency.REPEAT) {
            if (dto.installments() == null || dto.installments() < 2) {
                throw new IllegalArgumentException("Número de parcelas deve ser no mínimo 2 para transações REPEATs.");
            }

            if (dto.periodicity() == null) {
                throw new IllegalArgumentException("Periodicidade é obrigatória para transações REPEATs.");
            }
        }

        Account account = accountRepository.findById(dto.accounId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
            subcategory = subcategoryRepository.findById(dto.subcategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));
        }

        UUID groupId = UUID.randomUUID();
        int total = dto.frequency() == Frequency.REPEAT ? dto.installments() : 1;

        List<TransactionResponseDTO> responses = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            LocalDateTime releaseDateTime = dto.releaseDate();

            if (dto.frequency() == Frequency.REPEAT && dto.periodicity() != null) {
                releaseDateTime = calcularDataParcela(dto.releaseDate(), dto.periodicity(), i, dto.businessDayOnly());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setCategory(category);
            transaction.setSubcategory(subcategory);
            transaction.setName(dto.name());
            transaction.setType(dto.type());
            transaction.setStatus(dto.status());
            transaction.setReleaseDate(releaseDateTime);
            transaction.setValue(dto.value());
            transaction.setDescription(dto.description());
            transaction.setState(dto.state());
            transaction.setAdditionalInformation(dto.additionalInformation());
            transaction.setFrequency(dto.frequency());
            transaction.setInstallments(dto.installments());
            transaction.setPeriodicity(dto.periodicity());
            transaction.setBusinessDayOnly(dto.businessDayOnly());
            transaction.setInstallmentNumber(dto.frequency() == Frequency.REPEAT ? i + 1 : null);
            transaction.setRecurringGroupId(dto.frequency() == Frequency.REPEAT ? groupId : null);

            LocalDateTime now = LocalDateTime.now();
            transaction.setCreatedAt(now);
            transaction.setUpdatedAt(now);

            Transaction saved = transactionRepository.save(transaction);
            responses.add(toDTO(saved, false));
        }

        return responses;
    }

    private LocalDateTime calcularDataParcela(LocalDateTime dataInicial, Periodicity periodicidade, int parcelaIndex, Boolean diasUteis) {
        LocalDate data = dataInicial.toLocalDate();

        data = switch (periodicidade) {
            case DIARIO -> data.plusDays(parcelaIndex);
            case SEMANAL -> data.plusWeeks(parcelaIndex);
            case QUINZENAL -> data.plusWeeks(parcelaIndex * 2L);
            case MENSAL -> data.plusMonths(parcelaIndex);
            case TRIMESTRAL -> data.plusMonths(parcelaIndex * 3L);
            case SEMESTRAL -> data.plusMonths(parcelaIndex * 6L);
            case ANUAL -> data.plusYears(parcelaIndex);
        };

        if (Boolean.TRUE.equals(diasUteis)) {
            while (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                data = data.plusDays(1);
            }
        }

        return dataInicial.withYear(data.getYear()).withMonth(data.getMonthValue()).withDayOfMonth(data.getDayOfMonth());
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
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Override
    public TransactionResponseDTO updateState(UUID id, TransactionState state) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(TransactionNotFoundException::new);

        transaction.setState(state);
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        return toDTO(updated, false);
    }

    @Override
    public TransactionResponseDTO update(UUID id, TransactionRequestDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(TransactionNotFoundException::new);

        Account account = accountRepository.findById(dto.accounId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
            subcategory = subcategoryRepository.findById(dto.subcategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));
        }

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
        transaction.setPeriodicity(dto.periodicity());
        transaction.setBusinessDayOnly(dto.businessDayOnly());
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        return toDTO(updated, false);
    }

    @Override
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }

    private TransactionResponseDTO toDTO(Transaction transaction, boolean saldoNegativo) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getAccount().getAccountName(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getSubcategory() != null ? transaction.getSubcategory().getId() : null,
                transaction.getSubcategory() != null ? transaction.getSubcategory().getName() : null,
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
                transaction.getPeriodicity(),
                transaction.getBusinessDayOnly(),
                transaction.getInstallmentNumber(),
                transaction.getRecurringGroupId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                saldoNegativo
        );
    }

    @Override
    public List<AccountTransactionSummaryDTO> filtrarTransacoes(TransactionFilterDTO filtro) {
        List<Transaction> transacoes = transactionRepository.findAll().stream()
                .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || filtro.contaIds().contains(t.getAccount().getId()))
                .filter(t -> filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty() || filtro.categoriaIds().contains(t.getCategory().getId()))
                .filter(t -> (filtro.dataInicio() == null || !t.getReleaseDate().isBefore(filtro.dataInicio())) &&
                             (filtro.dataFim() == null || !t.getReleaseDate().isAfter(filtro.dataFim())))
                .collect(Collectors.toList());

        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getReleaseDate);
        if (filtro.ordenacao() != null) {
            switch (filtro.ordenacao()) {
                case DATA -> comparator = Comparator.comparing(Transaction::getReleaseDate);
                case CATEGORIA -> comparator = Comparator.comparing(t -> t.getCategory().getName(), String.CASE_INSENSITIVE_ORDER);
                case VALOR_CRESCENTE -> comparator = Comparator.comparing(Transaction::getValue);
                case VALOR_DECRESCENTE -> comparator = Comparator.comparing(Transaction::getValue).reversed();
            }
        }

        List<Transaction> transacoesOrdenadas = transacoes.stream()
                .sorted(comparator)
                .limit(10)
                .collect(Collectors.toList());

        return transacoesOrdenadas.stream()
                .map(t -> new AccountTransactionSummaryDTO(
                        t.getAccount().getAccountName(),
                        t.getType() == TransactionType.RECEITA ? t.getValue() : null,
                        null,
                        t.getType() == TransactionType.DESPESA ? t.getValue() : null,
                        null,
                        List.of(),
                        List.of()
                ))
                .collect(Collectors.toList());
    }
}
