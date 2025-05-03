package com.financeiro.api.repository;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.api.domain.enums.Status;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // tenta "advinhar" o nome da categoria e buscar por ela
    List<Category> findByNameContainingIgnoreCase(String name);

    List<Category> findByStatus(Status status);

    @Query("SELECT c.type FROM Category c WHERE c.id = :id")
    CategoryType findTypeById(@Param("id") UUID id);

    List<Category> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Category> findByAccountId(UUID accountId);

    List<Category> findAllByUserIdAndStatusIn(UUID userId, List<Status> statuses);

    List<Category> findAllByStatusIn(List<Status> statuses);

    List<Category> findAllByStatusInAndUser(List<Status> statuses, User user);

    List<Category> findAllByUser(User user);
}
