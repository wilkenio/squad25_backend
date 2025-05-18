package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.*;
import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSummaryDTO;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.AccountService;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

        private final AccountRepository accountRepository;
        private final CategoryRepository categoryRepository;
        private final TransactionRepository transactionRepository;

        public AccountServiceImpl(AccountRepository accountRepository, CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
                this.accountRepository = accountRepository;
                this.categoryRepository = categoryRepository;
                this.transactionRepository = transactionRepository;
        }

        // transformar no metodo GET
        @Override
        public AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto) {
                Double openingBalance = dto.openingBalance();
                Double specialCheck = dto.specialCheck();

                // Declaração das variáveis para serem implementadas depois
                Double receitas = 0.0;
                Double despesas = 0.0;
                Double receitasPrevistas = 0.0;
                Double despesasPrevistas = 0.0;

                Category category = categoryRepository.findById(dto.categoryId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

                // Cálculo do saldo atual
                Double saldo = 0.0;

                // Cálculo do saldo previsto considerando as previsões mensais
                Double saldoPrevisto = 0.0;

                // Cálculos totais incluindo o cheque especial
                Double receitaTotal = 0.0;
                Double despesaTotal = 0.0;
                Double saldoTotal = 0.0;

                // Salvando a conta no banco de dados antes de retornar
                Account account = new Account();
                account.setAccountName(dto.accountName());
                account.setAccountDescription(dto.accountDescription());
                account.setAdditionalInformation(dto.additionalInformation());
                account.setOpeningBalance(dto.openingBalance());
                account.setSpecialCheck(dto.specialCheck());
                account.setIncome(receitas);
                account.setExpense(despesas);
                account.setExpectedIncomeMonth(receitasPrevistas);
                account.setExpectedExpenseMonth(despesasPrevistas);
                account.setCategory(category);
                account.setStatus(Status.SIM);
                account.setCreatedAt(java.time.LocalDateTime.now());
                account.setUpdatedAt(java.time.LocalDateTime.now());

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

                // Cálculo do saldo atual
                Double currentBalance = account.getOpeningBalance() + account.getIncome() - account.getExpense();

                // Cálculo do saldo previsto considerando as previsões mensais
                Double expectedBalance = currentBalance + account.getSpecialCheck() +
                                account.getExpectedIncomeMonth() - account.getExpectedExpenseMonth();

                // Atualizando os dados básicos da conta
                account.setAccountName(dto.accountName());
                account.setAccountDescription(dto.accountDescription());
                account.setAdditionalInformation(dto.additionalInformation());
                account.setOpeningBalance(dto.openingBalance());
                account.setExpectedBalance(expectedBalance);
                account.setSpecialCheck(dto.specialCheck());
                account.setIncome(dto.income());
                account.setCurrentBalance(currentBalance);
                account.setExpense(dto.expense());
                account.setExpectedIncomeMonth(dto.expectedIncomeMonth());
                account.setExpectedExpenseMonth(dto.expectedExpenseMonth());
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
                                currentBalance,
                                expectedBalance,
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

        public List<AccountCalculationResponseDTO> findAll() {
                List<Status> statuses = List.of(Status.SIM, Status.NAO);
                return accountRepository.findAllByStatusIn(statuses).stream()
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

        public List<SummaryDTO> findSummary(List<UUID> accountsId, List<UUID> categoriesId, TransactionOrder order) {
                List<Account> accounts = accountRepository.findAllById(accountsId);
                List<Category> categories = categoryRepository.findAllById(categoriesId);

                // Mapear contas para AccountSummaryDTO
                List<AccountSummaryDTO> accountSummaries = accounts.stream()
                                .map(account -> new AccountSummaryDTO(
                                                account.getAccountName(),
                                                account.getIncome(),
                                                account.getExpectedIncomeMonth(),
                                                account.getExpense(),
                                                account.getExpectedExpenseMonth()))
                                .collect(Collectors.toList());

                // Mapear categorias para CategorySummaryDTO
                List<CategorySummaryDTO> categorySummaries = categories.stream()
                                .map(category -> new CategorySummaryDTO(
                                                category.getName(),
                                                category.getIconClass(),
                                                category.getColor(),
                                                category.getAdditionalInfo()))
                                .collect(Collectors.toList());

                // Buscar transações relacionadas às contas e categorias
                List<Transaction> transactions = transactionRepository.findByAccountInAndCategoryIn(accounts,
                                categories);

                // Ordenar transações de acordo com o TransactionOrder
                List<Transaction> orderedTransactions = switch (order) {
                        case VALOR_CRESCENTE -> transactions.stream()
                                        .sorted(Comparator.comparing(Transaction::getValue))
                                        .collect(Collectors.toList());
                        case VALOR_DECRESCENTE -> transactions.stream()
                                        .sorted(Comparator.comparing(Transaction::getValue).reversed())
                                        .collect(Collectors.toList());
                        case DATA -> transactions.stream()
                                        .sorted(Comparator.comparing(Transaction::getReleaseDate).reversed())
                                        .collect(Collectors.toList());
                        case CATEGORIA -> transactions.stream()
                                        .sorted(Comparator.comparing(t -> t.getCategory().getName()))
                                        .collect(Collectors.toList());
                        default -> transactions;
                };

                // Limitar a 10 transações e mapear para TransactionSummaryDTO
                List<TransactionSummaryDTO> transactionSummaries = orderedTransactions.stream()
                                .limit(10)
                                .map(transaction -> new TransactionSummaryDTO(
                                                transaction.getName(),
                                                transaction.getValue()))
                                .collect(Collectors.toList());

                // Criar e retornar o SummaryDTO
                return List.of(new SummaryDTO(
                                LocalDateTime.now().minusDays(30), // Período de 30 dias
                                LocalDateTime.now(),
                                accountSummaries,
                                categorySummaries,
                                transactionSummaries,
                                order));
        }
}