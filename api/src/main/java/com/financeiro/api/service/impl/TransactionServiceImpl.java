package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.infra.exceptions.TransactionNotFoundException;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    @Transactional 
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
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada com ID: " + dto.accountId()));

        Category category = null;
        if (dto.categoryId() != null) {
            category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + dto.categoryId()));
        }
        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
            subcategory = subcategoryRepository.findById(dto.subcategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada com ID: " + dto.subcategoryId()));
        }

        UUID groupId = UUID.randomUUID();
        int total = dto.frequency() == Frequency.REPEAT ? dto.installments() : 1;

        List<TransactionResponseDTO> responses = new ArrayList<>();
        User currentUser = getCurrentUser(); 

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
            transaction.setUser(currentUser); 
            LocalDateTime now = LocalDateTime.now();
            transaction.setCreatedAt(now);
            transaction.setUpdatedAt(now);

            Transaction saved = transactionRepository.save(transaction);

            if (account.getOpeningBalance() == null) account.setOpeningBalance(0.0);
            if (account.getCurrentBalance() == null) account.setCurrentBalance(account.getOpeningBalance());
            if (account.getIncome() == null) account.setIncome(0.0);
            if (account.getExpense() == null) account.setExpense(0.0);
            if (account.getExpectedIncomeMonth() == null) account.setExpectedIncomeMonth(0.0);
            if (account.getExpectedExpenseMonth() == null) account.setExpectedExpenseMonth(0.0);
            if (account.getSpecialCheck() == null) account.setSpecialCheck(0.0);

            if (dto.state() == TransactionState.EFFECTIVE) {

                if (dto.type() == TransactionType.RECEITA) {
                    account.setIncome(account.getIncome() + dto.value());
                } else if (dto.type() == TransactionType.DESPESA) {
                    account.setExpense(account.getExpense() + dto.value());
                }

                account.setCurrentBalance(
                    (account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0) +
                    account.getIncome() -
                    account.getExpense()
                );

            } else if (dto.state() == TransactionState.PENDING) {

                if (dto.type() == TransactionType.RECEITA) {
                    account.setExpectedIncomeMonth(account.getExpectedIncomeMonth() + dto.value());
                } else if (dto.type() == TransactionType.DESPESA) {
                    account.setExpectedExpenseMonth(account.getExpectedExpenseMonth() + dto.value());
                }
            }

            account.setExpectedBalance(
                (account.getCurrentBalance() != null ? account.getCurrentBalance() : 0.0) +
                (account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0) +
                account.getExpectedIncomeMonth() -
                account.getExpectedExpenseMonth()
            );

            account.setUpdatedAt(LocalDateTime.now()); 
            accountRepository.save(account); 

            responses.add(toDTO(saved, false)); 
        }

        return responses;
    }

    private LocalDateTime calcularDataParcela(LocalDateTime dataInicial, Periodicity periodicidade, int parcelaIndex,
            Boolean diasUteis) {
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

        return dataInicial.withYear(data.getYear()).withMonth(data.getMonthValue())
                .withDayOfMonth(data.getDayOfMonth());
    }

    @Override
    public List<TransactionSimplifiedResponseDTO> findAll(int page) {
        User currentUser = getCurrentUser();
        Status statusVisivel = Status.SIM; 
        
        Pageable pageable = PageRequest.of(page, 10, Sort.by("updatedAt").descending());

        Page<Transaction> pageResult = transactionRepository.findByUserAndStatus(currentUser, statusVisivel, pageable);

        return pageResult.getContent().stream()
                .map(transaction -> new TransactionSimplifiedResponseDTO(
                        transaction.getName(),
                        transaction.getType(),
                        transaction.getAccount() != null ? transaction.getAccount().getAccountName() : null, 
                        transaction.getReleaseDate(),
                        transaction.getFrequency(),
                        transaction.getValue()))
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Override
    public TransactionSimplifiedResponseDTO findById(UUID id) {
        User currentUser = getCurrentUser();
        Status statusVisivel = Status.SIM;

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada com ID: " + id)); 

        if (transaction.getUser() != null && 
            transaction.getUser().getId().equals(currentUser.getId()) &&
            transaction.getStatus() == statusVisivel) {
            
            return new TransactionSimplifiedResponseDTO(
                    transaction.getName(),
                    transaction.getType(),
                    transaction.getAccount() != null ? transaction.getAccount().getAccountName() : null,
                    transaction.getReleaseDate(),
                    transaction.getFrequency(),
                    transaction.getValue());
        } else {
            
            throw new TransactionNotFoundException("Transação não encontrada, excluída ou acesso negado. ID: " + id);
        }
    }

    @Override
    @Transactional 
    public TransactionResponseDTO updateState(UUID id, TransactionState newState) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada para atualização de estado. ID: " + id));

        Account account = transaction.getAccount();
        TransactionState oldState = transaction.getState();
        Double value = transaction.getValue();
        TransactionType type = transaction.getType();

        if (oldState == newState) {
            return toDTO(transaction, false);
        }

        transaction.setState(newState);
        transaction.setUpdatedAt(LocalDateTime.now());

        if (account.getOpeningBalance() == null) account.setOpeningBalance(0.0);
        if (account.getCurrentBalance() == null) account.setCurrentBalance(account.getOpeningBalance());
        if (account.getIncome() == null) account.setIncome(0.0);
        if (account.getExpense() == null) account.setExpense(0.0);
        if (account.getExpectedIncomeMonth() == null) account.setExpectedIncomeMonth(0.0);
        if (account.getExpectedExpenseMonth() == null) account.setExpectedExpenseMonth(0.0);
        if (account.getSpecialCheck() == null) account.setSpecialCheck(0.0);

        if (oldState == TransactionState.PENDING && newState == TransactionState.EFFECTIVE) {
            if (type == TransactionType.RECEITA) {
                account.setExpectedIncomeMonth(account.getExpectedIncomeMonth() - value); 
                account.setIncome(account.getIncome() + value);
            } else if (type == TransactionType.DESPESA) {
                account.setExpectedExpenseMonth(account.getExpectedExpenseMonth() - value); 
                account.setExpense(account.getExpense() + value); 
            }
        } 

        else if (oldState == TransactionState.EFFECTIVE && newState == TransactionState.PENDING) {
            if (type == TransactionType.RECEITA) {
                account.setIncome(account.getIncome() - value); 
                account.setExpectedIncomeMonth(account.getExpectedIncomeMonth() + value); 
            } else if (type == TransactionType.DESPESA) {
                account.setExpense(account.getExpense() - value); 
                account.setExpectedExpenseMonth(account.getExpectedExpenseMonth() + value); 
            }
        }

        account.setCurrentBalance(
            (account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0) +
            (account.getIncome() != null ? account.getIncome() : 0.0) -
            (account.getExpense() != null ? account.getExpense() : 0.0)
        );

        account.setExpectedBalance(
            (account.getCurrentBalance() != null ? account.getCurrentBalance() : 0.0) +
            (account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0) +
            (account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0) -
            (account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0)
        );
        
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account); 
        
        Transaction savedTransaction = transactionRepository.save(transaction); 

        return toDTO(savedTransaction, false);
    }

    @Override
    public TransactionResponseDTO update(UUID id, RecurringUpdateRequestDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(TransactionNotFoundException::new);

        Account account = accountRepository.findById(dto.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        Category category = null;
        if (dto.categoryId() != null) {
            category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + dto.categoryId()));
        }

        Subcategory subcategory = null;
        if (dto.subcategoryId() != null) {
            subcategory = subcategoryRepository.findById(dto.subcategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));
        }

        if (transaction.getRecurringGroupId() == null && dto.frequency() != Frequency.REPEAT) {
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

            return toDTO(transactionRepository.save(transaction), false);
        }

        if (transaction.getRecurringGroupId() == null && dto.frequency() == Frequency.REPEAT) {
            UUID groupId = UUID.randomUUID();
            int total = dto.installments();
            User currentUser = getCurrentUser(); 

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
            transaction.setFrequency(Frequency.REPEAT);
            transaction.setInstallments(dto.installments());
            transaction.setPeriodicity(dto.periodicity());
            transaction.setBusinessDayOnly(dto.businessDayOnly());
            transaction.setInstallmentNumber(1);
            transaction.setRecurringGroupId(groupId);
            transaction.setUpdatedAt(LocalDateTime.now());

            transactionRepository.save(transaction);

            for (int i = 1; i < total; i++) {
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
                nova.setUser(currentUser);

                transactionRepository.save(nova);
            }

            return toDTO(transaction, false);
        }

        if (transaction.getRecurringGroupId() != null) {
            atualizarRecorrenciaFutura(transaction.getRecurringGroupId(), dto);
            Transaction base = transactionRepository.findById(id)
                    .orElseThrow(TransactionNotFoundException::new);
            return toDTO(base, false);
        }

        throw new IllegalStateException("Não foi possível atualizar a transação.");
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
                transaction.getTransferGroupId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                saldoNegativo);
    }

    @Override
    public void cancelarRecorrencia(UUID recurringGroupId) {
        List<Transaction> transacoes = transactionRepository.findByRecurringGroupId(recurringGroupId);
        transactionRepository.deleteAll(transacoes);
    }

    @Override
    public void atualizarRecorrenciaFutura(UUID recurringGroupId, RecurringUpdateRequestDTO dto) {
        List<Transaction> transacoes = transactionRepository.findByRecurringGroupId(recurringGroupId);
        if (transacoes.isEmpty())
            return;

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
            base.setCategory(
                    dto.categoryId() != null ? categoryRepository.findById(dto.categoryId()).orElse(null)
                            : null);
            base.setSubcategory(
                    dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null)
                            : null);
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
            t.setCategory(
                    dto.categoryId() != null ? categoryRepository.findById(dto.categoryId()).orElse(null)
                            : null);
            t.setSubcategory(
                    dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null)
                            : null);
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
                LocalDateTime novaData = calcularDataParcela(dto.novaDataBase(), dto.periodicity(), parcela,
                        dto.businessDayOnly());
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
            User currentUser = getCurrentUser();
            for (int i = atuaisCount; i < novosTotal; i++) {
                Transaction nova = new Transaction();
                nova.setAccount(accountRepository.findById(dto.accountId()).orElseThrow());
                nova.setCategory(
                        dto.categoryId() != null ? categoryRepository.findById(dto.categoryId()).orElse(null)
                                : null);
                nova.setSubcategory(
                        dto.subcategoryId() != null ? subcategoryRepository.findById(dto.subcategoryId()).orElse(null)
                                : null);
                nova.setName(dto.name());
                nova.setType(dto.type());
                nova.setStatus(dto.status());
                nova.setReleaseDate(
                        calcularDataParcela(dto.novaDataBase(), dto.periodicity(), i, dto.businessDayOnly()));
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
                nova.setUser(currentUser);

                transactionRepository.save(nova);
            }
        }
    }

}
