package com.financeiro.api.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.*;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.service.AccountService;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

        private final AccountRepository accountRepository;
        private final CategoryRepository categoryRepository;

        public AccountServiceImpl(AccountRepository accountRepository, CategoryRepository categoryRepository) {
                this.accountRepository = accountRepository;
                this.categoryRepository = categoryRepository;
        }

        // transformar no metodo GET
        @Override
public AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto) {
        // ✅ Obtemos o usuário logado
        User currentUser = getCurrentUser();

        Double openingBalance = dto.openingBalance();
        Double specialCheck = dto.specialCheck();

        Double receitas = 0.0;
        Double despesas = 0.0;
        Double receitasPrevistas = 0.0;
        Double despesasPrevistas = 0.0;

        Category category = categoryRepository.findById(dto.categoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Double saldo = 0.0;
        Double saldoPrevisto = 0.0;
        Double receitaTotal = 0.0;
        Double despesaTotal = 0.0;
        Double saldoTotal = 0.0;

        Account account = new Account();
        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(openingBalance);
        account.setSpecialCheck(specialCheck);
        account.setIncome(receitas);
        account.setExpense(despesas);
        account.setExpectedIncomeMonth(receitasPrevistas);
        account.setExpectedExpenseMonth(despesasPrevistas);
        account.setCategory(category);
        account.setStatus(Status.SIM);
        account.setCreatedAt(java.time.LocalDateTime.now());
        account.setUpdatedAt(java.time.LocalDateTime.now());

        // ✅ Aqui associamos o usuário à conta
        account.setUser(currentUser);

        accountRepository.save(account);

        return new AccountCalculationResponseDTO(
                        account.getId(),
                        category.getId(),
                        category.getName(),
                        category.getIconClass(),
                        category.getColor(),
                        dto.accountName(),
                        dto.accountDescription(),
                        openingBalance,
                        specialCheck,
                        receitas,
                        despesas,
                        receitasPrevistas,
                        despesasPrevistas,
                        saldo,
                        saldoPrevisto,
                        receitaTotal,
                        despesaTotal,
                        saldoTotal);
}


        @Override
        public AccountTransactionResponseDTO update(UUID id, AccountTransactionRequestDTO dto) {
        Account account = accountRepository.findById(id).orElseThrow(
                        () -> new UserNotFoundException()); 

        Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(
                        () -> new EntityNotFoundException("Categoria não encontrada"));

        Double dtoOpeningBalance = dto.openingBalance() != null ? dto.openingBalance() : 0.0;
        Double dtoIncome = dto.income() != null ? dto.income() : 0.0;
        Double dtoExpense = dto.expense() != null ? dto.expense() : 0.0;
        Double dtoSpecialCheck = dto.specialCheck() != null ? dto.specialCheck() : 0.0;
        Double dtoExpectedIncomeMonth = dto.expectedIncomeMonth() != null ? dto.expectedIncomeMonth() : 0.0;
        Double dtoExpectedExpenseMonth = dto.expectedExpenseMonth() != null ? dto.expectedExpenseMonth() : 0.0;

        Double currentBalance = dtoOpeningBalance + dtoIncome - dtoExpense;

        Double expectedBalance = currentBalance + dtoSpecialCheck + dtoExpectedIncomeMonth - dtoExpectedExpenseMonth;

        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        
        account.setOpeningBalance(dtoOpeningBalance);
        account.setIncome(dtoIncome);
        account.setExpense(dtoExpense);
        account.setSpecialCheck(dtoSpecialCheck);
        account.setExpectedIncomeMonth(dtoExpectedIncomeMonth);
        account.setExpectedExpenseMonth(dtoExpectedExpenseMonth);
        
        account.setCurrentBalance(currentBalance); 
        account.setExpectedBalance(expectedBalance); 

        account.setStatus(dto.status());
        account.setCategory(category);
        account.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);

        return new AccountTransactionResponseDTO(
                        saved.getId(),
                        saved.getCategory().getId(),
                        saved.getCategory().getName(),
                        saved.getCategory().getIconClass(),
                        saved.getCategory().getColor(),
                        saved.getAccountName(),
                        saved.getAccountDescription(),
                        saved.getOpeningBalance(),
                        saved.getCurrentBalance(), 
                        saved.getExpectedBalance(), 
                        saved.getSpecialCheck(),
                        saved.getIncome(),
                        saved.getExpense(),
                        saved.getExpectedIncomeMonth(),
                        saved.getExpectedExpenseMonth(),
                        saved.getStatus());
        }

        // Método para atualizar a conta com base na transação
        public void updateAccountByTransaction(UUID accountId, TransactionType type, Double value) {
                Account account = accountRepository.findById(accountId)
                                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

                // Atualiza receita ou despesa com base no tipo de transação
                if (type == TransactionType.RECEITA) {
                        Double currentIncome = account.getIncome() != null ? account.getIncome() : 0.0;
                        account.setIncome(currentIncome + value);
                } else if (type == TransactionType.DESPESA) {
                        Double currentExpense = account.getExpense() != null ? account.getExpense() : 0.0;
                        account.setExpense(currentExpense + value);
                }

                // Recalcula o saldo atual
                Double currentBalance = account.getOpeningBalance() +
                                (account.getIncome() != null ? account.getIncome() : 0.0) -
                                (account.getExpense() != null ? account.getExpense() : 0.0);
                account.setCurrentBalance(currentBalance);

                account.setUpdatedAt(LocalDateTime.now());

                // Salva as alterações
                accountRepository.save(account);
        }

        @Override
        public void delete(UUID id) {
                Account account = accountRepository.findById(id).orElseThrow(
                                () -> new EntityNotFoundException());

                account.setStatus(Status.EXC);
                account.setUpdatedAt(LocalDateTime.now());
                accountRepository.save(account);
        }

        private User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return (User) authentication.getPrincipal();
        }

        public List<AccountCalculationResponseDTO> findAll() {
                List<Status> statuses = List.of(Status.SIM, Status.NAO);
                User currentUser = getCurrentUser();
            
                return accountRepository.findByUserAndStatusIn(currentUser, statuses).stream()
                    .map(acc -> {
                        Double saldoInicial = acc.getOpeningBalance();
                        Double chequeEspecial = acc.getSpecialCheck();
                        Double receitas = acc.getIncome() != null ? acc.getIncome() : 0.0;
                        Double despesas = acc.getExpense() != null ? acc.getExpense() : 0.0;
                        Double receitasPrevistas = acc.getExpectedIncomeMonth() != null ? acc.getExpectedIncomeMonth() : 0.0;
                        Double despesasPrevistas = acc.getExpectedExpenseMonth() != null ? acc.getExpectedExpenseMonth() : 0.0;
                        Double saldo = saldoInicial + receitas - despesas;
                        Double saldoPrevisto = saldo + chequeEspecial + receitasPrevistas - despesasPrevistas;
                        Double receitaTotal = receitas + receitasPrevistas;
                        Double despesaTotal = despesas + despesasPrevistas;
                        Double saldoTotal = saldoPrevisto + saldoInicial;
            
                        Category category = categoryRepository.findById(acc.getCategory().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
            
                        return new AccountCalculationResponseDTO(
                            acc.getId(),
                            category.getId(),
                            category.getName(),
                            category.getIconClass(),
                            category.getColor(),
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            saldoInicial,
                            chequeEspecial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                        );
                    }).collect(Collectors.toList());
            }
            

        @Override
        public AccountCalculationResponseDTO findById(UUID id) {
                Account account = accountRepository.findById(id).orElseThrow(
                                () -> new UserNotFoundException());

                Double saldoInicial = account.getOpeningBalance();
                Double chequeEspecial = account.getSpecialCheck();
                Double receitas = account.getIncome() != null ? account.getIncome() : 0.0;
                Double despesas = account.getExpense() != null ? account.getExpense() : 0.0;
                Double receitasPrevistas = account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth()
                                : 0.0;
                Double despesasPrevistas = account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth()
                                : 0.0;
                Double saldo = saldoInicial + receitas - despesas;
                Double saldoPrevisto = saldo + account.getSpecialCheck() + receitasPrevistas - despesasPrevistas;
                Double receitaTotal = receitas + receitasPrevistas;
                Double despesaTotal = despesas + despesasPrevistas;
                Double saldoTotal = saldoPrevisto + saldoInicial;

                Category category = categoryRepository.findById(account.getCategory().getId())
                                .orElseThrow(
                                                () -> new EntityNotFoundException("Categoria não encontrada"));

                return new AccountCalculationResponseDTO(
                                account.getId(),
                                category.getId(),
                                category.getName(),
                                category.getIconClass(),
                                category.getColor(),
                                account.getAccountName(),
                                account.getAccountDescription(),
                                saldoInicial,
                                chequeEspecial,
                                receitas,
                                despesas,
                                receitasPrevistas,
                                despesasPrevistas,
                                saldo,
                                saldoPrevisto,
                                receitaTotal,
                                despesaTotal,
                                saldoTotal);
        }

        @Override
        public List<AccountCalculationResponseDTO> findByAccountName(String accountName) {
                return accountRepository.findByAccountNameContainingIgnoreCase(accountName).stream()
                                .map(acc -> {
                                        Double saldoInicial = acc.getOpeningBalance();
                                        Double chequeEspecial = acc.getSpecialCheck();
                                        Double receitas = acc.getIncome() != null ? acc.getIncome() : 0.0;
                                        Double despesas = acc.getExpense() != null ? acc.getExpense() : 0.0;
                                        Double receitasPrevistas = acc.getExpectedIncomeMonth() != null
                                                        ? acc.getExpectedIncomeMonth()
                                                        : 0.0;
                                        Double despesasPrevistas = acc.getExpectedExpenseMonth() != null
                                                        ? acc.getExpectedExpenseMonth()
                                                        : 0.0;
                                        Double saldo = saldoInicial + receitas - despesas;
                                        Double saldoPrevisto = saldo + acc.getSpecialCheck() + receitasPrevistas
                                                        - despesasPrevistas;
                                        Double receitaTotal = receitas + receitasPrevistas;
                                        Double despesaTotal = despesas + despesasPrevistas;
                                        Double saldoTotal = saldoPrevisto + saldoInicial;

                                        Category category = categoryRepository.findById(acc.getCategory().getId())
                                                        .orElseThrow(
                                                                        () -> new EntityNotFoundException(
                                                                                        "Categoria não encontrada"));

                                        return new AccountCalculationResponseDTO(
                                                        acc.getId(),
                                                        category.getId(),
                                                        category.getName(),
                                                        category.getIconClass(),
                                                        category.getColor(),
                                                        acc.getAccountName(),
                                                        acc.getAccountDescription(),
                                                        saldoInicial,
                                                        chequeEspecial,
                                                        receitas,
                                                        despesas,
                                                        receitasPrevistas,
                                                        despesasPrevistas,
                                                        saldo,
                                                        saldoPrevisto,
                                                        receitaTotal,
                                                        despesaTotal,
                                                        saldoTotal);
                                }).collect(Collectors.toList());
        }

        @Override
        public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue) {
                return accountRepository.findByOpeningBalanceBetween(minValue, maxValue).stream()
                                .map(acc -> {
                                        Double saldoInicial = acc.getOpeningBalance();
                                        Double chequeEspecial = acc.getSpecialCheck();
                                        Double receitas = acc.getIncome() != null ? acc.getIncome() : 0.0;
                                        Double despesas = acc.getExpense() != null ? acc.getExpense() : 0.0;
                                        Double receitasPrevistas = acc.getExpectedIncomeMonth() != null
                                                        ? acc.getExpectedIncomeMonth()
                                                        : 0.0;
                                        Double despesasPrevistas = acc.getExpectedExpenseMonth() != null
                                                        ? acc.getExpectedExpenseMonth()
                                                        : 0.0;
                                        Double saldo = saldoInicial + receitas - despesas;
                                        Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                                        Double receitaTotal = receitas + receitasPrevistas;
                                        Double despesaTotal = despesas + despesasPrevistas;
                                        Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                                        Category category = categoryRepository.findById(acc.getCategory().getId())
                                                        .orElseThrow(
                                                                        () -> new EntityNotFoundException(
                                                                                        "Categoria não encontrada"));

                                        return new AccountCalculationResponseDTO(
                                                        acc.getId(),
                                                        category.getId(),
                                                        category.getName(),
                                                        category.getIconClass(),
                                                        category.getColor(),
                                                        acc.getAccountName(),
                                                        acc.getAccountDescription(),
                                                        saldoInicial,
                                                        chequeEspecial,
                                                        receitas,
                                                        despesas,
                                                        receitasPrevistas,
                                                        despesasPrevistas,
                                                        saldo,
                                                        saldoPrevisto,
                                                        receitaTotal,
                                                        despesaTotal,
                                                        saldoTotal);
                                }).collect(Collectors.toList());
        }

        @Override
        public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue) {
                return accountRepository.findBySpecialCheckBetween(minValue, maxValue).stream()
                                .map(acc -> {
                                        Double saldoInicial = acc.getOpeningBalance();
                                        Double chequeEspecial = acc.getSpecialCheck();
                                        Double receitas = acc.getIncome() != null ? acc.getIncome() : 0.0;
                                        Double despesas = acc.getExpense() != null ? acc.getExpense() : 0.0;
                                        Double receitasPrevistas = acc.getExpectedIncomeMonth() != null
                                                        ? acc.getExpectedIncomeMonth()
                                                        : 0.0;
                                        Double despesasPrevistas = acc.getExpectedExpenseMonth() != null
                                                        ? acc.getExpectedExpenseMonth()
                                                        : 0.0;
                                        Double saldo = saldoInicial + receitas - despesas;
                                        Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                                        Double receitaTotal = receitas + receitasPrevistas;
                                        Double despesaTotal = despesas + despesasPrevistas;
                                        Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                                        Category category = categoryRepository.findById(acc.getCategory().getId())
                                                        .orElseThrow(
                                                                        () -> new EntityNotFoundException(
                                                                                        "Categoria não encontrada"));

                                        return new AccountCalculationResponseDTO(
                                                        acc.getId(),
                                                        category.getId(),
                                                        category.getName(),
                                                        category.getIconClass(),
                                                        category.getColor(),
                                                        acc.getAccountName(),
                                                        acc.getAccountDescription(),
                                                        saldoInicial,
                                                        chequeEspecial,
                                                        receitas,
                                                        despesas,
                                                        receitasPrevistas,
                                                        despesasPrevistas,
                                                        saldo,
                                                        saldoPrevisto,
                                                        receitaTotal,
                                                        despesaTotal,
                                                        saldoTotal);
                                }).collect(Collectors.toList());
        }

        @Override
        public List<AccountCalculationResponseDTO> findByStatus(Status status) {
                return accountRepository.findByStatus(status).stream()
                                .map(acc -> {
                                        Double saldoInicial = acc.getOpeningBalance();
                                        Double chequeEspecial = acc.getSpecialCheck();
                                        Double receitas = acc.getIncome() != null ? acc.getIncome() : 0.0;
                                        Double despesas = acc.getExpense() != null ? acc.getExpense() : 0.0;
                                        Double receitasPrevistas = acc.getExpectedIncomeMonth() != null
                                                        ? acc.getExpectedIncomeMonth()
                                                        : 0.0;
                                        Double despesasPrevistas = acc.getExpectedExpenseMonth() != null
                                                        ? acc.getExpectedExpenseMonth()
                                                        : 0.0;
                                        Double saldo = saldoInicial + receitas - despesas;
                                        Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                                        Double receitaTotal = receitas + receitasPrevistas;
                                        Double despesaTotal = despesas + despesasPrevistas;
                                        Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                                        Category category = categoryRepository.findById(acc.getCategory().getId())
                                                        .orElseThrow(
                                                                        () -> new EntityNotFoundException(
                                                                                        "Categoria não encontrada"));

                                        return new AccountCalculationResponseDTO(
                                                        acc.getId(),
                                                        category.getId(),
                                                        category.getName(),
                                                        category.getIconClass(),
                                                        category.getColor(),
                                                        acc.getAccountName(),
                                                        acc.getAccountDescription(),
                                                        saldoInicial,
                                                        chequeEspecial,
                                                        receitas,
                                                        despesas,
                                                        receitasPrevistas,
                                                        despesasPrevistas,
                                                        saldo,
                                                        saldoPrevisto,
                                                        receitaTotal,
                                                        despesaTotal,
                                                        saldoTotal);
                                }).collect(Collectors.toList());
        }
}