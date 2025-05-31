package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

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
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new UserNotFoundException("Usuário não autenticado ou não encontrado na sessão.");
        }
        return (User) authentication.getPrincipal();
    }

    @Override
    @Transactional
    public AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto) {
        User currentUser = getCurrentUser();
        Double openingBalance = (dto.openingBalance() != null) ? dto.openingBalance() : 0.0;
        Double specialCheck = (dto.specialCheck() != null) ? dto.specialCheck() : 0.0;
        Double receitas = 0.0;
        Double despesas = 0.0;
        Double receitasPrevistas = 0.0;
        Double despesasPrevistas = 0.0;

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + dto.categoryId()));

        Double currentBalance = openingBalance + receitas - despesas; 
        Double expectedBalance = currentBalance + specialCheck + receitasPrevistas - despesasPrevistas;

        Account account = new Account();
        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(openingBalance);
        account.setSpecialCheck(specialCheck);
        account.setIncome(receitas);
        account.setExpense(despesas);
        account.setCurrentBalance(currentBalance); 
        account.setExpectedIncomeMonth(receitasPrevistas);
        account.setExpectedExpenseMonth(despesasPrevistas);
        account.setExpectedBalance(expectedBalance); 
        account.setCategory(category);
        account.setStatus(Status.SIM); 
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account.setUser(currentUser);

        Account savedAccount = accountRepository.save(account);

        return mapAccountToCalculationResponseDTO(savedAccount); 
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

        Double currentBalance = (newOpeningBalance != null ? newOpeningBalance : 0.0) + incomeToUse - expenseToUse;

        Double expectedBalance = currentBalance + specialCheckToUse + expectedIncomeMonthToUse - expectedExpenseMonthToUse;

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

        account.setCurrentBalance(currentBalance);
        account.setExpectedBalance(expectedBalance);

        account.setUpdatedAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        return new AccountTransactionResponseDTO(
                savedAccount.getId(),
                savedAccount.getCategory().getId(),
                savedAccount.getCategory().getName(),
                savedAccount.getCategory().getIconClass(),
                savedAccount.getCategory().getColor(),
                savedAccount.getAccountName(),
                savedAccount.getAccountDescription(),
                savedAccount.getOpeningBalance(),
                savedAccount.getCurrentBalance(),
                savedAccount.getExpectedBalance(),
                savedAccount.getSpecialCheck(),
                savedAccount.getIncome(),
                savedAccount.getExpense(),
                savedAccount.getExpectedIncomeMonth(),
                savedAccount.getExpectedExpenseMonth(),
                savedAccount.getStatus()
        );
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
    public List<AccountCalculationResponseDTO> findAll() {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO); 
        return accountRepository.findByUserAndStatusInOrderByCreatedAtDesc(currentUser, statusesVisiveis)
                .stream()
                .map(this::mapAccountToCalculationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountCalculationResponseDTO findById(UUID id) {
        User currentUser = getCurrentUser();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada com ID: " + id));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Acesso negado à conta com ID: " + id);
        }
        if (account.getStatus() == Status.EXC) {
            throw new EntityNotFoundException("Conta com ID: " + id + " foi excluída e não pode ser acessada.");
        }

        return mapAccountToCalculationResponseDTO(account);
    }
    
    @Override
    public List<AccountCalculationResponseDTO> findByAccountName(String accountName) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByAccountNameContainingIgnoreCase(accountName).stream()
                .filter(acc -> acc.getUser().getId().equals(currentUser.getId()) && statusesVisiveis.contains(acc.getStatus()))
                .map(this::mapAccountToCalculationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findByOpeningBalanceBetween(minValue, maxValue).stream()
                .filter(acc -> acc.getUser().getId().equals(currentUser.getId()) && statusesVisiveis.contains(acc.getStatus()))
                .map(this::mapAccountToCalculationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue) {
        User currentUser = getCurrentUser();
        List<Status> statusesVisiveis = List.of(Status.SIM, Status.NAO);
        return accountRepository.findBySpecialCheckBetween(minValue, maxValue).stream()
                .filter(acc -> acc.getUser().getId().equals(currentUser.getId()) && statusesVisiveis.contains(acc.getStatus()))
                .map(this::mapAccountToCalculationResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByStatus(Status status) {
        User currentUser = getCurrentUser();

        if (status == Status.EXC) {

            return accountRepository.findByUserAndStatusIn(currentUser, Collections.singletonList(Status.EXC)).stream()
                .map(this::mapAccountToCalculationResponseDTO)
                .collect(Collectors.toList());
        }
        return accountRepository.findByUserAndStatusIn(currentUser, Collections.singletonList(status)).stream()
            .map(this::mapAccountToCalculationResponseDTO)
            .collect(Collectors.toList());
    }

    private AccountCalculationResponseDTO mapAccountToCalculationResponseDTO(Account acc) {

        Double openingBalance = acc.getOpeningBalance() != null ? acc.getOpeningBalance() : 0.0;
        Double specialCheck = acc.getSpecialCheck() != null ? acc.getSpecialCheck() : 0.0;
        Double income = acc.getIncome() != null ? acc.getIncome() : 0.0;
        Double expense = acc.getExpense() != null ? acc.getExpense() : 0.0;
        Double expectedIncomeMonth = acc.getExpectedIncomeMonth() != null ? acc.getExpectedIncomeMonth() : 0.0;
        Double expectedExpenseMonth = acc.getExpectedExpenseMonth() != null ? acc.getExpectedExpenseMonth() : 0.0;

        Double currentBalanceInEntity = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : (openingBalance + income - expense);
        Double expectedBalanceInEntity = acc.getExpectedBalance() != null ? acc.getExpectedBalance() : (currentBalanceInEntity + specialCheck + expectedIncomeMonth - expectedExpenseMonth);

        Double totalIncome = income + expectedIncomeMonth;
        Double totalExpense = expense + expectedExpenseMonth;

        Double totalBalance = expectedBalanceInEntity; 

        Category category = acc.getCategory(); 

        UUID categoryId = null;
        String categoryName = null;
        String iconClass = null;
        String color = null;
        if (category != null) {
            categoryId = category.getId();
            categoryName = category.getName();
            iconClass = category.getIconClass();
            color = category.getColor();
        } else {

        }

        return new AccountCalculationResponseDTO(
                acc.getId(),
                categoryId,
                categoryName,
                iconClass,
                color,
                acc.getAccountName(),
                acc.getAccountDescription(),
                openingBalance,     
                specialCheck,       
                income,           
                expense,        
                expectedIncomeMonth, 
                expectedExpenseMonth,
                currentBalanceInEntity, 
                expectedBalanceInEntity,
                totalIncome,        
                totalExpense,        
                totalBalance         
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
        if (account.getCurrentBalance() == null) account.setCurrentBalance(account.getOpeningBalance() + account.getIncome() - account.getExpense()); 
        if (account.getSpecialCheck() == null) account.setSpecialCheck(0.0);
        if (account.getExpectedIncomeMonth() == null) account.setExpectedIncomeMonth(0.0);
        if (account.getExpectedExpenseMonth() == null) account.setExpectedExpenseMonth(0.0);
        if (account.getExpectedBalance() == null) { 
             account.setExpectedBalance(
                account.getCurrentBalance() + 
                account.getSpecialCheck() + 
                account.getExpectedIncomeMonth() - 
                account.getExpectedExpenseMonth()
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

        Double currentBalance = openingBalance + income - expense;
        account.setCurrentBalance(currentBalance);

        Double expectedBalance = currentBalance + specialCheck + expectedIncomeMonth - expectedExpenseMonth;
        account.setExpectedBalance(expectedBalance);
    }
}