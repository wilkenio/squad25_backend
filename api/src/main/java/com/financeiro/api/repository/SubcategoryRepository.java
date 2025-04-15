package com.financeiro.api.repository;

import com.financeiro.api.domain.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {
    List<Subcategory> findByCategoryIdAndCategoryUserId(UUID categoryId, UUID userId);
}
