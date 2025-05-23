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

        Account account = accountRepository.findById(dto.accountId())
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

        Account account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
            subcategory = subcategoryRepository.findById(dto.subcategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));
        }

        // Detectar mudança de NON_RECURRING para REPEAT
        if (transaction.getFrequency() == Frequency.NON_RECURRING && dto.frequency() == Frequency.REPEAT) {
            UUID groupId = UUID.randomUUID();
            int total = dto.installments();
            List<Transaction> novas = new ArrayList<>();

            for (int i = 0; i < total; i++) {
                LocalDateTime releaseDate = dto.releaseDate();
                if (dto.periodicity() != null) {
                    releaseDate = calcularDataParcela(dto.releaseDate(), dto.periodicity(), i, dto.businessDayOnly());
                }

                Transaction nova = new Transaction();
                nova.setAccount(account);
                nova.setCategory(category);
                nova.setSubcategory(subcategory);
                nova.setName(dto.name());
                nova.setType(dto.type());
                nova.setStatus(dto.status());
                nova.setReleaseDate(releaseDate);
                nova.setValue(dto.value());
                nova.setDescription(dto.description());
                nova.setState(dto.state());
                nova.setAdditionalInformation(dto.additionalInformation());
                nova.setFrequency(Frequency.REPEAT);
                nova.setInstallments(dto.installments());
                nova.setPeriodicity(dto.periodicity());
                nova.setBusinessDayOnly(dto.businessDayOnly());
                nova.setInstallmentNumber(i + 1);
                nova.setRecurringGroupId(groupId);
                nova.setCreatedAt(LocalDateTime.now());
                nova.setUpdatedAt(LocalDateTime.now());

                novas.add(nova);
            }

            transactionRepository.delete(transaction);
            transactionRepository.saveAll(novas);
            return toDTO(novas.get(0), false); // Retorna a primeira parcela
        }

        // Caso normal (sem conversão para REPEAT)
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
    public void cancelarRecorrencia(UUID recurringGroupId) {
        List<Transaction> transacoes = transactionRepository.findByRecurringGroupId(recurringGroupId);
        transactionRepository.deleteAll(transacoes);
    }

    @Override
    public void atualizarRecorrenciaFutura(UUID recurringGroupId, RecurringUpdateRequestDTO dto) {
        List<Transaction> transacoes = transactionRepository.findByRecurringGroupId(recurringGroupId);
        if (transacoes.isEmpty()) return;

        transacoes.sort(Comparator.comparing(Transaction::getReleaseDate));

        LocalDateTime agora = LocalDateTime.now();
        List<Transaction> futuras = transacoes.stream()
                .filter(t -> t.getReleaseDate().isAfter(agora))
                .toList();

        Transaction base = transacoes.stream()
                .filter(t -> !t.getReleaseDate().isAfter(agora))
                .max(Comparator.comparing(Transaction::getReleaseDate))
                .orElse(futuras.isEmpty() ? transacoes.get(0) : futuras.get(0));

        if (dto.frequency() == Frequency.NON_RECURRING) {

            base.setAccount(accountRepository.findById(dto.accountId()).orElseThrow());
            base.setCategory(categoryRepository.findById(dto.categoryId()).orElseThrow());
            base.setSubcategory(dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null) : null);
            base.setName(dto.name());
            base.setType(dto.type());
            base.setStatus(dto.status());
            base.setReleaseDate(dto.releaseDate());
            base.setValue(dto.value());
            base.setDescription(dto.description());
            base.setState(dto.state());
            base.setAdditionalInformation(dto.additionalInformation());
            base.setFrequency(Frequency.NON_RECURRING);
            base.setInstallments(null);
            base.setPeriodicity(null);
            base.setBusinessDayOnly(null);
            base.setInstallmentNumber(null);
            base.setRecurringGroupId(null);
            base.setUpdatedAt(LocalDateTime.now());

            List<Transaction> transacoesParaExcluir = transacoes.stream()
                    .filter(t -> !t.getId().equals(base.getId()))
                    .toList();
            transactionRepository.deleteAll(transacoesParaExcluir);
            transactionRepository.save(base);

            return;
        }

        int novosTotal = dto.installments();
        List<Transaction> atuais = transacoes.stream()
                .filter(t -> t.getInstallmentNumber() != null)
                .toList();

        int atuaisCount = atuais.size();

        for (Transaction t : futuras) {
            t.setAccount(accountRepository.findById(dto.accountId()).orElseThrow());
            t.setCategory(categoryRepository.findById(dto.categoryId()).orElseThrow());
            t.setSubcategory(dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null) : null);
            t.setName(dto.name());
            t.setType(dto.type());
            t.setStatus(dto.status());
            t.setValue(dto.value());
            t.setDescription(dto.description());
            t.setState(dto.state());
            t.setAdditionalInformation(dto.additionalInformation());
            t.setFrequency(dto.frequency());
            t.setInstallments(dto.installments());
            t.setPeriodicity(dto.periodicity());
            t.setBusinessDayOnly(dto.businessDayOnly());
            t.setUpdatedAt(LocalDateTime.now());

            if (dto.novaDataBase() != null && t.getInstallmentNumber() != null) {
                int parcela = t.getInstallmentNumber() - 1;
                LocalDateTime novaData = calcularDataParcela(dto.novaDataBase(), dto.periodicity(), parcela, dto.businessDayOnly());
                t.setReleaseDate(novaData);
            }

            transactionRepository.save(t);
        }

        if (atuaisCount > novosTotal) {
            List<Transaction> excedentes = transacoes.stream()
                    .filter(t -> t.getInstallmentNumber() != null && t.getInstallmentNumber() > novosTotal)
                    .toList();
            transactionRepository.deleteAll(excedentes);
        }

        if (atuaisCount < novosTotal) {
            UUID groupId = recurringGroupId;
            for (int i = atuaisCount; i < novosTotal; i++) {
                Transaction nova = new Transaction();
                nova.setAccount(accountRepository.findById(dto.accountId()).orElseThrow());
                nova.setCategory(categoryRepository.findById(dto.categoryId()).orElseThrow());
                nova.setSubcategory(dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null) : null);
                nova.setName(dto.name());
                nova.setType(dto.type());
                nova.setStatus(dto.status());
                nova.setReleaseDate(calcularDataParcela(dto.novaDataBase(), dto.periodicity(), i, dto.businessDayOnly()));
                nova.setValue(dto.value());
                nova.setDescription(dto.description());
                nova.setState(dto.state());
                nova.setAdditionalInformation(dto.additionalInformation());
                nova.setFrequency(dto.frequency());
                nova.setInstallments(dto.installments());
                nova.setPeriodicity(dto.periodicity());
                nova.setBusinessDayOnly(dto.businessDayOnly());
                nova.setInstallmentNumber(i + 1);
                nova.setRecurringGroupId(groupId);
                nova.setCreatedAt(LocalDateTime.now());
                nova.setUpdatedAt(LocalDateTime.now());

                transactionRepository.save(nova);
            }
        }
    }
}
