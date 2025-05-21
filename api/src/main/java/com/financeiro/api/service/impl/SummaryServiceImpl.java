package com.financeiro.api.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.SummaryDTO;
import com.financeiro.api.dto.accountDTO.AccountSummaryDTO;
import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategorySummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSummaryDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.SummaryService;

@Service
public class SummaryServiceImpl implements SummaryService {

        private final AccountRepository accountRepository;
        private final CategoryRepository categoryRepository;
        private final TransactionRepository transactionRepository;

        public SummaryServiceImpl(AccountRepository accountRepository, CategoryRepository categoryRepository,
                        TransactionRepository transactionRepository) {
                this.accountRepository = accountRepository;
                this.categoryRepository = categoryRepository;
                this.transactionRepository = transactionRepository;
        }

        @Override
        public List<SummaryDTO> findSummary(List<UUID> accountsId, List<UUID> categoriesId, TransactionOrder order,
                        LocalDateTime startDate, LocalDateTime endDate) {
                List<Account> accounts = accountRepository.findAllById(accountsId);
                List<Category> categories = categoryRepository.findAllById(categoriesId);

                // Buscar transações relacionadas às contas e categorias
                List<Transaction> transactions = transactionRepository.findByAccountInAndCategoryIn(accounts,
                                categories);

                // Filtrar transações pelo período especificado
                transactions = transactions.stream()
                                .filter(transaction -> !transaction.getReleaseDate().isBefore(startDate) &&
                                                !transaction.getReleaseDate().isAfter(endDate))
                                .collect(Collectors.toList());

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
                                                transaction.getPeriodicity(),
                                                transaction.getBusinessDayOnly(),
                                                transaction.getInstallmentNumber(),
                                                transaction.getRecurringGroupId(),
                                                List.of(new AccountSummaryDTO(
                                                                transaction.getAccount().getId(),
                                                                transaction.getAccount().getAccountName(),
                                                                transaction.getAccount().getAccountDescription(),
                                                                transaction.getAccount().getAdditionalInformation(),
                                                                transaction.getAccount().getOpeningBalance(),
                                                                transaction.getAccount().getCurrentBalance(),
                                                                transaction.getAccount().getExpectedBalance(),
                                                                transaction.getAccount().getSpecialCheck(),
                                                                transaction.getAccount().getIncome(),
                                                                transaction.getAccount().getExpense(),
                                                                transaction.getAccount().getExpectedIncomeMonth(),
                                                                transaction.getAccount().getExpectedExpenseMonth(),
                                                                transaction.getAccount().getStatus())),
                                                List.of(new CategorySummaryDTO(
                                                                transaction.getCategory().getId(),
                                                                transaction.getCategory().getName(),
                                                                transaction.getCategory().getType(),
                                                                transaction.getCategory().getIconClass(),
                                                                transaction.getCategory().getColor(),
                                                                transaction.getCategory().getAdditionalInfo(),
                                                                transaction.getCategory().getStatus())),
                                                transaction.getSubcategory() != null
                                                                ? List.of(new SubcategorySummaryDTO(
                                                                                transaction.getSubcategory().getId(),
                                                                                transaction.getSubcategory().getName(),
                                                                                transaction.getSubcategory()
                                                                                                .getStandardRecommendation(),
                                                                                transaction.getSubcategory()
                                                                                                .getIconClass(),
                                                                                transaction.getSubcategory()
                                                                                                .getStatus(),
                                                                                transaction.getSubcategory().getColor(),
                                                                                transaction.getSubcategory()
                                                                                                .getAdditionalInfo()))
                                                                : List.of()))
                                .collect(Collectors.toList());

                // Criar e retornar o SummaryDTO
                return List.of(new SummaryDTO(
                                startDate,
                                endDate,
                                transactionSummaries,
                                order));
        }
}
