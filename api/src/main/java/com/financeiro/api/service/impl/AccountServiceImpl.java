package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionResponseDTO;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            throw new UserNotFoundException("Usuário não autenticado ou não encontrado na sessão.");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            logger.error("Principal de autenticação não é uma instância da classe User. Principal é: {}", principal.getClass().getName());
            throw new IllegalStateException("O principal de autenticação não é uma instância da classe User esperada.");
        }
        return (User) principal;
    }

    @Override
    @Transactional
    public AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto) {
        User currentUser = getCurrentUser();
        Double openingBalance = (dto.openingBalance() != null) ? dto.openingBalance() : 0.0;
        Double specialCheck = (dto.specialCheck() != null) ? dto.specialCheck() : 0.0;
        
        Double receitasAcumuladas = 0.0;
        Double despesasAcumuladas = 0.0;
        Double receitasPrevistasAcumuladas = 0.0;
        Double despesasPrevistasAcumuladas = 0.0;

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + dto.categoryId()));

        Double currentBalanceGeral = openingBalance + receitasAcumuladas - despesasAcumuladas;
        Double expectedBalanceGeral = currentBalanceGeral + specialCheck + receitasPrevistasAcumuladas - despesasPrevistasAcumuladas;

        Account account = new Account();
        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(openingBalance);
        account.setSpecialCheck(specialCheck);
        account.setIncome(receitasAcumuladas); 
        account.setExpense(despesasAcumuladas); 
        account.setCurrentBalance(currentBalanceGeral); 
        account.setExpectedIncomeMonth(receitasPrevistasAcumuladas); 
        account.setExpectedExpenseMonth(despesasPrevistasAcumuladas); 
        account.setExpectedBalance(expectedBalanceGeral); 
        account.setCategory(category);
        account.setStatus(Status.SIM);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account.setUser(currentUser);

        Account savedAccount = accountRepository.save(account);
        
        LocalDate today = LocalDate.now();
        return mapAccountToCalculationResponseDTO(savedAccount, today.getYear(), today.getMonthValue());
    }

    @Override
    @Transactional
    public AccountTransactionResponseDTO update(UUID id, AccountTransactionRequestDTO dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada com ID: " + id));

        User currentUser = getCurrentUser();
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Acesso negado para atualizar a conta com ID: " + id);
        }
        
        Category category = categoryRepository.findById(dto.categoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + dto.categoryId()));

        Double newOpeningBalance = (dto.openingBalance() != null) ? dto.openingBalance() : account.getOpeningBalance();

        Double incomeToUse = (dto.income() != null) ? dto.income() : (account.getIncome() != null ? account.getIncome() : 0.0);
        Double expenseToUse = (dto.expense() != null) ? dto.expense() : (account.getExpense() != null ? account.getExpense() : 0.0);
        Double specialCheckToUse = (dto.specialCheck() != null) ? dto.specialCheck() : (account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0);
        Double expectedIncomeMonthToUse = (dto.expectedIncomeMonth() != null) ? dto.expectedIncomeMonth() : (account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0);
        Double expectedExpenseMonthToUse = (dto.expectedExpenseMonth() != null) ? dto.expectedExpenseMonth() : (account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0);

        Double currentBalanceGeral = (newOpeningBalance != null ? newOpeningBalance : 0.0) + incomeToUse - expenseToUse;

        Double expectedBalanceGeral = currentBalanceGeral + specialCheckToUse + expectedIncomeMonthToUse - expectedExpenseMonthToUse;

        if (dto.accountName() != null) account.setAccountName(dto.accountName());
        if (dto.accountDescription() != null) account.setAccountDescription(dto.accountDescription());
        if (dto.additionalInformation() != null) account.setAdditionalInformation(dto.additionalInformation());
        if (dto.status() != null) account.setStatus(dto.status());
        
        account.setCategory(category);
        account.setOpeningBalance(newOpeningBalance);
        account.setIncome(incomeToUse); 
        account.setExpense(expenseToUse); 
        account.setSpecialCheck(specialCheckToUse);
        account.setExpectedIncomeMonth(expectedIncomeMonthToUse); 
        account.setExpectedExpenseMonth(expectedExpenseMonthToUse); 
        account.setCurrentBalance(currentBalanceGeral);
        account.setExpectedBalance(expectedBalanceGeral); 
        account.setUpdatedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        return new AccountTransactionResponseDTO(
                savedAccount.getId(), savedAccount.getCategory().getId(), savedAccount.getCategory().getName(),
                savedAccount.getCategory().getIconClass(), savedAccount.getCategory().getColor(),
                savedAccount.getAccountName(), savedAccount.getAccountDescription(), savedAccount.getOpeningBalance(),
                savedAccount.getCurrentBalance(), savedAccount.getExpectedBalance(), savedAccount.getSpecialCheck(),
                savedAccount.getIncome(), savedAccount.getExpense(), savedAccount.getExpectedIncomeMonth(),
                savedAccount.getExpectedExpenseMonth(), savedAccount.getStatus());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada ao tentar deletar com ID: " + id));
        User currentUser = getCurrentUser();
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Acesso negado para deletar a conta com ID: " + id);
        }
        List<Status> statusesAtivosParaTransacao = List.of(Status.SIM, Status.NAO);
        List<Transaction> transacoesDaConta = transactionRepository.findByAccountAndStatusIn(account, statusesAtivosParaTransacao);
        if (transacoesDaConta != null && !transacoesDaConta.isEmpty()) {
            for (Transaction transaction : transacoesDaConta) {
                transaction.setStatus(Status.EXC);
                transaction.setUpdatedAt(LocalDateTime.now());
            }
            transactionRepository.saveAll(transacoesDaConta);
        }
        account.setStatus(Status.EXC);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public List<AccountCalculationResponseDTO> findAll(Integer year, Integer month) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByUserAndStatusInOrderByCreatedAtDesc(currentUser, statusesVisiveis)
                .stream()
                .map(account -> mapAccountToCalculationResponseDTO(account, year, month))
                .collect(Collectors.toList());
    }

    @Override
    public AccountCalculationResponseDTO findById(UUID id, Integer year, Integer month) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada com ID: " + id));
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Acesso negado à conta com ID: " + id);
        }
        if (account.getStatus() == Status.EXC) {
            throw new EntityNotFoundException("Conta com ID: " + id + " foi excluída e não pode ser acessada.");
        }
        return mapAccountToCalculationResponseDTO(account, year, month);
    }
    
    @Override
    public List<AccountCalculationResponseDTO> findByAccountName(String accountName, Integer year, Integer month) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByUserAndAccountNameContainingIgnoreCaseAndStatusIn(currentUser, accountName, statusesVisiveis)
                .stream()
                .map(acc -> mapAccountToCalculationResponseDTO(acc, year, month))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue, Integer year, Integer month) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByUserAndOpeningBalanceBetweenAndStatusIn(currentUser, minValue, maxValue, statusesVisiveis)
                .stream()
                .map(acc -> mapAccountToCalculationResponseDTO(acc, year, month))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue, Integer year, Integer month) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByUserAndSpecialCheckBetweenAndStatusIn(currentUser, minValue, maxValue, statusesVisiveis)
                .stream()
                .map(acc -> mapAccountToCalculationResponseDTO(acc, year, month))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByStatus(Status status, Integer year, Integer month) {
        User currentUser = getCurrentUser();
        if (status == Status.EXC) {
            return accountRepository.findByUserAndStatusIn(currentUser, Collections.singletonList(Status.EXC)).stream()
                .map(acc -> mapAccountToCalculationResponseDTO(acc, year, month))
                .collect(Collectors.toList());
        }
        return accountRepository.findByUserAndStatusIn(currentUser, Collections.singletonList(status)).stream()
            .map(acc -> mapAccountToCalculationResponseDTO(acc, year, month))
            .collect(Collectors.toList());
    }

private AccountCalculationResponseDTO mapAccountToCalculationResponseDTO(Account acc, Integer year, Integer month) {
    int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
    int effectiveMonth = (month != null) ? month : LocalDate.now().getMonthValue();

    LocalDate firstDayOfCurrentDisplayMonth = LocalDate.of(effectiveYear, effectiveMonth, 1);

    LocalDateTime endOfPreviousMonth = firstDayOfCurrentDisplayMonth.minusDays(1).atTime(LocalTime.MAX);

    List<Transaction> historicalEffectiveTransactions = transactionRepository.findByAccountAndStatusAndStateAndReleaseDateLessThanEqual(
            acc, Status.SIM, TransactionState.EFFECTIVE, endOfPreviousMonth
    );

    double totalHistoricalEffectiveIncome = 0.0;
    double totalHistoricalEffectiveExpense = 0.0;

    for (Transaction t : historicalEffectiveTransactions) {

            if (t.getType() == TransactionType.RECEITA) {
                totalHistoricalEffectiveIncome += (t.getValue() != null ? t.getValue() : 0.0);
            } else if (t.getType() == TransactionType.DESPESA) {
                totalHistoricalEffectiveExpense += (t.getValue() != null ? t.getValue() : 0.0);
            }
        // }
    }

    Double dtoSaldoInicialDinamico = (acc.getOpeningBalance() != null ? acc.getOpeningBalance() : 0.0) +
                                   totalHistoricalEffectiveIncome -
                                   totalHistoricalEffectiveExpense;


    LocalDate initialDateOfMonth = LocalDate.of(effectiveYear, effectiveMonth, 1);
    LocalDateTime startDateOfMonth = initialDateOfMonth.atStartOfDay();
    LocalDateTime endDateOfMonth = initialDateOfMonth.withDayOfMonth(initialDateOfMonth.lengthOfMonth()).atTime(LocalTime.MAX);

    List<Transaction> monthlyTransactions = transactionRepository.findByAccountAndStatusAndReleaseDateBetween(
            acc, Status.SIM, startDateOfMonth, endDateOfMonth);

    double receitasEfetivasDoMes = 0.0;
    double despesasEfetivasDoMes = 0.0;
    double receitasPendentesDoMes = 0.0;
    double despesasPendentesDoMes = 0.0;

    for (Transaction t : monthlyTransactions) {
        if (t.getState() == TransactionState.EFFECTIVE) {
            if (t.getType() == TransactionType.RECEITA) {
                receitasEfetivasDoMes += (t.getValue() != null ? t.getValue() : 0.0);
            } else if (t.getType() == TransactionType.DESPESA) {
                despesasEfetivasDoMes += (t.getValue() != null ? t.getValue() : 0.0);
            }
        } else if (t.getState() == TransactionState.PENDING) {
            if (t.getType() == TransactionType.RECEITA) {
                receitasPendentesDoMes += (t.getValue() != null ? t.getValue() : 0.0);
            } else if (t.getType() == TransactionType.DESPESA) {
                despesasPendentesDoMes += (t.getValue() != null ? t.getValue() : 0.0);
            }
        }
    }

    Double dtoReceitasMensaisEfetivas = receitasEfetivasDoMes;
    Double dtoDespesasMensaisEfetivas = despesasEfetivasDoMes;
    
    Double dtoTotalReceitasConsideradasNoMes = receitasEfetivasDoMes + receitasPendentesDoMes;
    Double dtoTotalDespesasConsideradasNoMes = despesasEfetivasDoMes + despesasPendentesDoMes;
    
    Double specialCheckGeral = acc.getSpecialCheck() != null ? acc.getSpecialCheck() : 0.0;

    Double dtoSaldoMesCorrente = dtoSaldoInicialDinamico + dtoReceitasMensaisEfetivas - dtoDespesasMensaisEfetivas;

    Double dtoSaldoPrevistoMesCorrente = dtoSaldoInicialDinamico + specialCheckGeral + dtoTotalReceitasConsideradasNoMes - dtoTotalDespesasConsideradasNoMes;

    Category category = acc.getCategory();
    UUID categoryId = category != null ? category.getId() : null;
    String categoryName = category != null ? category.getName() : null;
    String iconClass = category != null ? category.getIconClass() : null;
    String color = category != null ? category.getColor() : null;

    return new AccountCalculationResponseDTO(
            acc.getId(), categoryId, categoryName, iconClass, color,
            acc.getAccountName(), acc.getAccountDescription(),
            
            dtoSaldoInicialDinamico,            
            specialCheckGeral,                 
            
            dtoReceitasMensaisEfetivas,         
            dtoDespesasMensaisEfetivas,         
            
            dtoTotalReceitasConsideradasNoMes,  
            dtoTotalDespesasConsideradasNoMes,  
            
            dtoSaldoMesCorrente,                
            dtoSaldoPrevistoMesCorrente         
            
    );
}

    @Transactional
    public void updateAccountByTransaction(UUID accountId, TransactionType type, Double value, TransactionState transactionState) {
        Account account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada para atualização por transação. ID: " + accountId));
        
        initializeAccountBalancesForUpdate(account); 

        if (transactionState == TransactionState.EFFECTIVE) {
            if (type == TransactionType.RECEITA) {
                account.setIncome(account.getIncome() + value); 
            } else if (type == TransactionType.DESPESA) {
                account.setExpense(account.getExpense() + value); 
            }
        } else if (transactionState == TransactionState.PENDING) {
            if (type == TransactionType.RECEITA) {
                account.setExpectedIncomeMonth(account.getExpectedIncomeMonth() + value); 
            } else if (type == TransactionType.DESPESA) {
                account.setExpectedExpenseMonth(account.getExpectedExpenseMonth() + value); 
            }
        }
        
        recalculateAllAccountBalances(account); 

        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }
    
    private void initializeAccountBalancesForUpdate(Account account) {
        if (account.getOpeningBalance() == null) account.setOpeningBalance(0.0);
        if (account.getIncome() == null) account.setIncome(0.0);
        if (account.getExpense() == null) account.setExpense(0.0);
        if (account.getCurrentBalance() == null) { 
            account.setCurrentBalance(
                (account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0) +
                (account.getIncome() != null ? account.getIncome() : 0.0) -
                (account.getExpense() != null ? account.getExpense() : 0.0)
            );
        }
        if (account.getSpecialCheck() == null) account.setSpecialCheck(0.0);
        if (account.getExpectedIncomeMonth() == null) account.setExpectedIncomeMonth(0.0);
        if (account.getExpectedExpenseMonth() == null) account.setExpectedExpenseMonth(0.0);
        if (account.getExpectedBalance() == null) {
             account.setExpectedBalance(
                (account.getCurrentBalance() != null ? account.getCurrentBalance() : 0.0) + 
                (account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0) + 
                (account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0) - 
                (account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0)
            );
        }
    }

    private void recalculateAllAccountBalances(Account account) {
        Double openingBalance = account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0;
        Double income = account.getIncome() != null ? account.getIncome() : 0.0; 
        Double expense = account.getExpense() != null ? account.getExpense() : 0.0; 
        Double specialCheck = account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0;
        Double expectedIncomeMonth = account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0; 
        Double expectedExpenseMonth = account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0; 

        Double currentBalanceGeral = openingBalance + income - expense;
        account.setCurrentBalance(currentBalanceGeral);

        Double expectedBalanceGeral = currentBalanceGeral + specialCheck + expectedIncomeMonth - expectedExpenseMonth;
        account.setExpectedBalance(expectedBalanceGeral);
    }
}