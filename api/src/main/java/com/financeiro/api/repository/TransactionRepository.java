package com.financeiro.api.repository;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User; 
import com.financeiro.api.domain.enums.Frequency;
import com.financeiro.api.domain.enums.Status; 
import com.financeiro.api.domain.enums.TransactionState; 
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByCategoryId(UUID categoryId);
    List<Transaction> findByFrequency(Frequency frequency);
    boolean existsByNameAndAccountIdAndReleaseDateBetween(String name, UUID accountId, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByAccountInAndCategoryIn(List<Account> accounts, List<Category> categories);
    List<Transaction> findByRecurringGroupId(UUID recurringGroupId);
    List<Transaction> findByTransferGroupId(UUID transferGroupId);
    List<Transaction> findByAccountAndStatusIn(Account account, List<Status> statuses); 
    List<Transaction> findAllByStateAndStatusAndReleaseDateLessThanEqual( 
       TransactionState state,
       Status status,
       LocalDateTime releaseDate
    );

    Page<Transaction> findByUserAndStatus(User user, Status status, Pageable pageable);
}