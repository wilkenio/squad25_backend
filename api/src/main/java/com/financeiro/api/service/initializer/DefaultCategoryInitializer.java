package com.financeiro.api.service.initializer;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultCategoryInitializer {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public DefaultCategoryInitializer(CategoryRepository categoryRepository, SubcategoryRepository subcategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    public void createDefaultCategoriesForUser(User user) {
        createCategoryWithSubcategories("Casa", "bi-house", "#3498db", CategoryType.EXPENSE, user, List.of(
                "Água", "Energia elétrica", "Gás", "Internet", "Condomínio",
                "Aluguel", "Manutenção", "IPTU", "Seguro residencial"
        ));

        createCategoryWithSubcategories("Alimentação", "bi-utensils", "#e74c3c", CategoryType.EXPENSE, user, List.of(
                "Supermercado", "Feira", "Restaurantes", "Delivery", "Lanches"
        ));

        createCategoryWithSubcategories("Transporte", "bi-car-front", "#2980b9", CategoryType.EXPENSE, user, List.of(
                "Combustível", "Transporte público", "Aplicativos", "Estacionamento",
                "Pedágio", "Manutenção do veículo", "Seguro do veículo", "IPVA", "Financiamento do carro"
        ));

        createCategoryWithSubcategories("Contas e Assinaturas", "bi-receipt", "#9b59b6", CategoryType.EXPENSE, user, List.of(
                "Celular", "Streaming"
        ));

        // ==== REVENUE CATEGORIES ====

        createCategoryWithSubcategoriesStatusNAO("Contas", "bi-bank", "#2ecc71", CategoryType.REVENUE, user, List.of(
                "NuBank", "Caixa", "Banco do Brasil", "Bradesco", "Itaú", "Santander"
        ));

        createCategoryWithSubcategories("Salário e Benefícios", "bi-wallet", "#2ecc71", CategoryType.REVENUE, user, List.of(
                "Salário Base", "Bônus", "Comissões", "13º Salário", "Vale-refeição", "Reembolso de despesas"
        ));

        createCategoryWithSubcategories("Investimentos", "bi-pie-chart", "#27ae60", CategoryType.REVENUE, user, List.of(
                "Dividendos", "Juros de Poupança", "Juros de CDB", "Ganho de Capital", "Aluguel de imóveis"
        ));

        createCategoryWithSubcategories("Receitas Financeiras", "bi-coins", "#2980b9", CategoryType.REVENUE, user, List.of(
                "Juros de Conta Corrente", "Previdência Privada", "Rendimentos de Criptomoedas"
        ));

        createCategoryWithSubcategories("Outras Receitas", "bi-gift", "#9b59b6", CategoryType.REVENUE, user, List.of(
                "Cashback", "Royalties", "Presentes"
        ));

        
    }

    private void createCategoryWithSubcategories(String categoryName, String icon, String color, CategoryType type, User user, List<String> subNames) {
        Category category = new Category();
        category.setName(categoryName);
        category.setIconClass(icon);
        category.setColor(color);
        category.setType(type);
        category.setStandardRecommendation(true);
        category.setStatus(Status.SIM);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        for (String sub : subNames) {
            Subcategory subcategory = new Subcategory();
            subcategory.setName(sub);
            subcategory.setCategory(savedCategory);
            subcategory.setIconClass("bi-circle");
            subcategory.setColor("#bdc3c7");
            subcategory.setStatus(Status.SIM);
            subcategory.setStandardRecommendation(true);
            subcategory.setCreatedAt(LocalDateTime.now());
            subcategory.setUpdatedAt(LocalDateTime.now());
            subcategoryRepository.save(subcategory);
        }
    }

    private void createCategoryWithSubcategoriesStatusNAO(String categoryName, String icon, String color, CategoryType type, User user, List<String> subNames) {
        Category category = new Category();
        category.setName(categoryName);
        category.setIconClass(icon);
        category.setColor(color);
        category.setType(type);
        category.setStandardRecommendation(true);
        category.setStatus(Status.NAO);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        for (String sub : subNames) {
            Subcategory subcategory = new Subcategory();
            subcategory.setName(sub);
            subcategory.setCategory(savedCategory);
            subcategory.setIconClass("bi-circle");
            subcategory.setColor("#bdc3c7");
            subcategory.setStatus(Status.NAO);
            subcategory.setStandardRecommendation(true);
            subcategory.setCreatedAt(LocalDateTime.now());
            subcategory.setUpdatedAt(LocalDateTime.now());
            subcategoryRepository.save(subcategory);
        }
    }
} 
