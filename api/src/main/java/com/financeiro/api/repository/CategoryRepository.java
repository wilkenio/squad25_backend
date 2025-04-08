package com.financeiro.api.repository;

import com.financeiro.api.domain.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.api.domain.enums.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    // tenta "advinhar" o nome da categoria e buscar por ela
    List<Category> findByNameContainingIgnoreCase(String name);
    
    List<Category> findByStatus(Status status);
    
    List<Category> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
