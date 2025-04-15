package com.financeiro.api.repository;

import com.financeiro.api.domain.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import com.financeiro.api.domain.enums.*;

import java.util.UUID;
import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {
    List<Subcategory> findAllByStatusIn(List<Status> statuses);
    List<Subcategory> findByCategoryIdAndCategoryUserIdAndStatusIn(UUID categoryId, UUID userId, List<Status> statuses);
}
