package com.financeiro.api.service.initializer;

import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.categoryDTO.CategoryConfig;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultCategoryInitializer {

        private final CategoryRepository categoryRepository;
        private final SubcategoryRepository subcategoryRepository;

        public DefaultCategoryInitializer(CategoryRepository categoryRepository,
                        SubcategoryRepository subcategoryRepository) {
                this.categoryRepository = categoryRepository;
                this.subcategoryRepository = subcategoryRepository;
        }

        private final List<CategoryConfig> expenseCategories = List.of(
                        new CategoryConfig("Casa", "bi-house", "#3498db", CategoryType.EXPENSE,
                                        List.of("Água", "Energia elétrica", "Gás", "Internet", "Condomínio",
                                                        "Aluguel", "Manutenção", "IPTU", "Seguro residencial")),
                        new CategoryConfig("Alimentação", "bi-utensils", "#e74c3c", CategoryType.EXPENSE,
                                        List.of("Supermercado", "Feira", "Restaurantes", "Delivery", "Lanches")),
                        new CategoryConfig("Transporte", "bi-car-front", "#2980b9", CategoryType.EXPENSE,
                                        List.of("Combustível", "Transporte público", "Aplicativos", "Estacionamento",
                                                        "Pedágio", "Manutenção do veículo", "Seguro do veículo", "IPVA",
                                                        "Financiamento do carro")),
                        new CategoryConfig("Contas e Assinaturas", "bi-receipt", "#9b59b6", CategoryType.EXPENSE,
                                        List.of("Celular", "Streaming")));

        private final List<CategoryConfig> revenueCategories = List.of(
                        new CategoryConfig("Contas", "bi-bank", "#2ecc71", CategoryType.REVENUE,
                                        List.of("NuBank", "Caixa", "Banco do Brasil", "Bradesco", "Itaú", "Santander")),
                        new CategoryConfig("Salário e Benefícios", "bi-wallet", "#2ecc71", CategoryType.REVENUE,
                                        List.of("Salário Base", "Bônus", "Comissões", "13º Salário", "Vale-refeição",
                                                        "Reembolso de despesas")),
                        new CategoryConfig("Investimentos", "bi-pie-chart", "#27ae60", CategoryType.REVENUE,
                                        List.of("Dividendos", "Juros de Poupança", "Juros de CDB", "Ganho de Capital",
                                                        "Aluguel de imóveis")),
                        new CategoryConfig("Receitas Financeiras", "bi-coins", "#2980b9", CategoryType.REVENUE,
                                        List.of("Juros de Conta Corrente", "Previdência Privada",
                                                        "Rendimentos de Criptomoedas")),
                        new CategoryConfig("Outras Receitas", "bi-gift", "#9b59b6", CategoryType.REVENUE,
                                        List.of("Cashback", "Royalties", "Presentes")));

        @Transactional
        public void createDefaultCategoriesForUser(User user) {
                LocalDateTime now = LocalDateTime.now();
                List<Category> categories = new ArrayList<>();
                List<Subcategory> subcategories = new ArrayList<>();

                // Processa todas as categorias
                List.of(expenseCategories, revenueCategories)
                                .forEach(categoryList -> categoryList.forEach(config -> {
                                        Category category = createCategory(config, user, now);
                                        categories.add(category);
                                        subcategories.addAll(
                                                        createSubcategories(category, config.subcategories(), now));
                                }));

                // Salva todas as categorias e subcategorias em lote
                categoryRepository.saveAll(categories);
                subcategoryRepository.saveAll(subcategories);
        }

        private Category createCategory(CategoryConfig config, User user, LocalDateTime timestamp) {
                Category category = new Category();
                category.setName(config.name());
                category.setIconClass(config.icon());
                category.setColor(config.color());
                category.setType(config.type());
                category.setStandardRecommendation(true);
                category.setStatus(Status.SIM);
                category.setCreatedAt(timestamp);
                category.setUpdatedAt(timestamp);
                category.setUser(user);
                return category;
        }

        private List<Subcategory> createSubcategories(Category category, List<String> subNames,
                        LocalDateTime timestamp) {
                return subNames.stream().map(name -> {
                        Subcategory subcategory = new Subcategory();
                        subcategory.setName(name);
                        subcategory.setCategory(category);
                        subcategory.setIconClass("bi-circle");
                        subcategory.setColor("#bdc3c7");
                        subcategory.setStatus(Status.SIM);
                        subcategory.setStandardRecommendation(true);
                        subcategory.setCreatedAt(timestamp);
                        subcategory.setUpdatedAt(timestamp);
                        return subcategory;
                }).toList();
        }
}
