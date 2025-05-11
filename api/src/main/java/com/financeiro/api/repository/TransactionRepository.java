package com.financeiro.api.repository;

import com.financeiro.api.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.api.domain.enums.Status;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
     List<Transaction> findByCategoryId(UUID categoryId);

     List<Transaction> findAllByStatusIn(List<Status> statuses);
}
