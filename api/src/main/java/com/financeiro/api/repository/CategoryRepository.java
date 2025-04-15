package com.financeiro.api.repository;

import com.financeiro.api.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import com.financeiro.api.domain.enums.*;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByName(String name);
    List<Category> findAllByUserIdAndStatusIn(UUID userId, List<Status> statuses );
}
