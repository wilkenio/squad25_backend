package com.financeiro.api.service.initializer;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.SubcategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultInitializer {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final AccountRepository accountRepository;

    public DefaultInitializer(CategoryRepository categoryRepository,
                                      SubcategoryRepository subcategoryRepository,
                                      AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.accountRepository = accountRepository;
    }

    public void createDefaultAccountAndCategoriesForUser(User user) {
        createCategoryWithSubcategories("Casa", "bi-house", "#3498db", CategoryType.EXPENSE, user, List.of(
                "Água", "Energia elétrica", "Gás", "Internet", "Condomínio",
                "Aluguel", "Manutenção", "IPTU", "Seguro residencial"
        ));

        createCategoryWithSubcategories("Alimentação", "bi-fork-knife", "#e74c3c", CategoryType.EXPENSE, user, List.of(
                "Supermercado", "Restaurante", "Delivery"
        ));

        createCategoryWithSubcategories("Transporte", "bi-car-front", "#2980b9", CategoryType.EXPENSE, user, List.of(
                "Combustível", "Transporte público", "Aplicativos", "Estacionamento",
                "Pedágio", "Manutenção do veículo", "Seguro do veículo", "IPVA", "Financiamento do carro"
        ));

        createCategoryWithSubcategories("Contas e Assinaturas", "bi-receipt", "#9b59b6", CategoryType.EXPENSE, user, List.of(
                "Celular", "Streaming"
        ));

        // Categorias de contas
        Category accountCategory = createCategoryWithSubcategories("Conta Corrente", "bi-wallet2", "#1f77b4", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Conta Poupança", "bi-piggy-bank", "#2ca02c", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Carteira", "bi-cash", "#ff851b", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Cartão de Crédito", "bi-credit-card", "#d62728", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Conta Salário", "bi-currency-dollar", "#008080", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Conta Investimento", "bi-graph-up", "#9467bd", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Conta Digital", "bi-phone", "#00b894", CategoryType.ACCOUNT, user, List.of());
        createCategoryWithSubcategories("Conta Conjunta", "bi-people", "#ff69b4", CategoryType.ACCOUNT, user, List.of());

        createCategoryWithSubcategories("Salário e Benefícios", "bi-wallet", "#2ecc71", CategoryType.REVENUE, user, List.of(
                "Salário", "Bônus", "Comissões", "13º Salário", "Vale-refeição", "Reembolso de despesas"
        ));

        createCategoryWithSubcategories("Investimentos", "bi-pie-chart", "#27ae60", CategoryType.REVENUE, user, List.of(
                "Dividendos", "Juros de Poupança", "Juros de CDB", "Ganho de Capital", "Aluguel de imóveis"
        ));

        createCategoryWithSubcategories("Receitas Financeiras", "bi-currency-dollar", "#103030", CategoryType.REVENUE, user, List.of(
                "Juros de Conta Corrente", "Previdência Privada", "Rendimentos de Criptomoedas"
        ));

        createCategoryWithSubcategories("Outras Receitas", "bi-gift", "#9b59b6", CategoryType.REVENUE, user, List.of(
                "Cashback", "Royalties"
        ));

        // Criar contas padrão (receita e despesa)
        createDefaultAccount("Conta Receita Padrão", "Conta usada para lançamento de receitas padrão", 0.0, 0.0, user, accountCategory);
        createDefaultAccount("Conta Despesa Padrão", "Conta usada para lançamento de despesas padrão", 0.0, 0.0, user, accountCategory);
    }

    private Category createCategoryWithSubcategories(String categoryName, String icon, String color,
                                                     CategoryType type, User user, List<String> subNames) {
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

        return savedCategory;
    }

    private void createDefaultAccount(String name, String description, Double openingBalance,
                                      Double specialCheck, User user, Category category) {
        Account account = new Account();
        account.setAccountName(name);
        account.setAccountDescription(description);
        account.setOpeningBalance(openingBalance != null ? openingBalance : 0.0); 
        account.setSpecialCheck(specialCheck != null ? specialCheck : 0.0);     
        
        account.setIncome(0.0);
        account.setExpense(0.0);
        account.setCurrentBalance(openingBalance != null ? openingBalance : 0.0); 
        account.setExpectedIncomeMonth(0.0);
        account.setExpectedExpenseMonth(0.0);
        account.setExpectedBalance((openingBalance != null ? openingBalance : 0.0) + (specialCheck != null ? specialCheck : 0.0));
        account.setUser(user);
        account.setCategory(category);
        account.setStatus(Status.SIM); //
        account.setCreatedAt(LocalDateTime.now()); //
        account.setUpdatedAt(LocalDateTime.now()); //

        accountRepository.save(account);
    }

}
