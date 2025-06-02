package com.financeiro.api.repository;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUser(User user);
    List<Account> findByUserAndStatusInOrderByCreatedAtDesc(User user, List<Status> statuses);
    List<Account> findByUserAndStatusIn(User user, List<Status> statuses);
    List<Account> findByAccountNameContainingIgnoreCase(String accountName);
    List<Account> findByOpeningBalanceBetween(Double minValue, Double maxValue);
    List<Account> findBySpecialCheckBetween(Double minValue, Double maxValue);
    List<Account> findByStatus(Status status); 
    List<Account> findAllByStatusIn(List<Status> statuses); 

    List<Account> findByUserAndAccountNameContainingIgnoreCaseAndStatusIn(
            User user, 
            String accountName, 
            List<Status> statuses
    );

    List<Account> findByUserAndOpeningBalanceBetweenAndStatusIn(
            User user, 
            Double minValue, 
            Double maxValue, 
            List<Status> statuses
    );

    List<Account> findByUserAndSpecialCheckBetweenAndStatusIn(
            User user, 
            Double minValue, 
            Double maxValue, 
            List<Status> statuses
    );
}