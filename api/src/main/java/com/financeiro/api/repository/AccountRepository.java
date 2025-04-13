package com.financeiro.api.repository;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByAccountNameContainingIgnoreCase(String accountName);
    List<Account> findByOpeningBalanceBetween(Double minValue, Double maxValue);
    List<Account> findBySpecialCheckBetween(Double minValue, Double maxValue);
    List<Account> findByStatus(Status status);
}
