package com.financeiro.api.repository;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.Frequency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
     List<Transaction> findByCategoryId(UUID categoryId);
     List<Transaction> findByFrequency(Frequency frequency);
     boolean existsByNameAndAccountIdAndReleaseDateBetween(String name, UUID accountId, LocalDateTime start,
               LocalDateTime end);

     List<Transaction> findByAccountInAndCategoryIn(List<Account> accounts, List<Category> categories);
     List<Transaction> findByRecurringGroupId(UUID recurringGroupId);
}
