package com.financeiro.api.repository.template;

import com.financeiro.api.domain.template.SubcategoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubcategoryTemplateRepository extends JpaRepository<SubcategoryTemplate, UUID> {
}
