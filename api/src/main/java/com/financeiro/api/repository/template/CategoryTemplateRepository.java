package com.financeiro.api.repository.template;

import com.financeiro.api.domain.template.CategoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryTemplateRepository extends JpaRepository<CategoryTemplate, UUID> {
}
