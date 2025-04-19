package com.financeiro.api.service;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DefaultCategoryInitializer {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public DefaultCategoryInitializer(CategoryRepository categoryRepository, SubcategoryRepository subcategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Transactional
    public void createDefaultCategoriesForUser(User user) {
        Map<String, List<String>> defaultData = Map.of(
            "Casa", List.of("Água", "Energia elétrica", "Gás", "Internet", "Condomínio", "Aluguel", "Manutenção", "IPTU", "Seguro residencial"),
            "Alimentação", List.of("Supermercado", "Feira", "Restaurantes", "Delivery", "Lanches"),
            "Transporte", List.of("Combustível", "Transporte público", "Aplicativos", "Estacionamento", "Pedágio", "Manutenção do veículo", "Seguro do veículo", "IPVA", "Financiamento do carro"),
            "Contas e Assinaturas", List.of("Celular", "Streaming")
        );

        LocalDateTime now = LocalDateTime.now();

        defaultData.forEach((categoryName, subcategoryNames) -> {
            Category category = new Category();
            category.setName(categoryName);
            category.setUser(user);
            category.setColor("#cccccc");
            category.setIconClass("default-icon");
            category.setStatus(Status.SIM);
            category.setStandardRecommendation(true);
            category.setType(CategoryType.EXPENSE);
            category.setCreatedAt(now);
            category.setUpdatedAt(now);

            category = categoryRepository.save(category);

            for (String subName : subcategoryNames) {
                Subcategory sub = new Subcategory();
                sub.setName(subName);
                sub.setCategory(category);
                sub.setStatus(Status.SIM);
                sub.setStandardRecommendation(true);
                sub.setColor("#dddddd");
                sub.setIconClass("default-sub-icon");
                sub.setCreatedAt(now);
                sub.setUpdatedAt(now);

                subcategoryRepository.save(sub);
            }
        });
    }
}
