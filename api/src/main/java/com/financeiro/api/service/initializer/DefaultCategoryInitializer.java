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
public class DefaultCategoryInitializer {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final AccountRepository accountRepository;

    public DefaultCategoryInitializer(CategoryRepository categoryRepository,
                                      SubcategoryRepository subcategoryRepository,
                                      AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.accountRepository = accountRepository;
    }

    public void createDefaultCategoriesForUser(User user) {
        Account contaDespesa = createContaPadrao(user, "Conta Padrão - Despesas");
        Account contaReceita = createContaPadrao(user, "Conta Padrão - Receitas");

        createCategoryWithSubcategories("Casa", "bi-house", "#3498db", CategoryType.EXPENSE, user, contaDespesa, List.of(
                "Água", "Energia elétrica", "Gás", "Internet", "Condomínio",
                "Aluguel", "Manutenção", "IPTU", "Seguro residencial"
        ));

        createCategoryWithSubcategories("Alimentação", "bi-utensils", "#e74c3c", CategoryType.EXPENSE, user, contaDespesa, List.of(
                "Supermercado", "Feira", "Restaurantes", "Delivery", "Lanches"
        ));

        createCategoryWithSubcategories("Transporte", "bi-car-front", "#2980b9", CategoryType.EXPENSE, user, contaDespesa, List.of(
                "Combustível", "Transporte público", "Aplicativos", "Estacionamento",
                "Pedágio", "Manutenção do veículo", "Seguro do veículo", "IPVA", "Financiamento do carro"
        ));

        createCategoryWithSubcategories("Contas e Assinaturas", "bi-receipt", "#9b59b6", CategoryType.EXPENSE, user, contaDespesa, List.of(
                "Celular", "Streaming"
        ));

        // ==== REVENUE CATEGORIES ====

        createCategoryWithSubcategories("Contas", "bi-bank", "#2ecc71", CategoryType.REVENUE, user, contaReceita, List.of(
                "NuBank", "Caixa", "Banco do Brasil", "Bradesco", "Itaú", "Santander"
        ));

        createCategoryWithSubcategories("Salário e Benefícios", "bi-wallet", "#2ecc71", CategoryType.REVENUE, user, contaReceita, List.of(
                "Salário Base", "Bônus", "Comissões", "13º Salário", "Vale-refeição", "Reembolso de despesas"
        ));

        createCategoryWithSubcategories("Investimentos", "bi-pie-chart", "#27ae60", CategoryType.REVENUE, user, contaReceita, List.of(
                "Dividendos", "Juros de Poupança", "Juros de CDB", "Ganho de Capital", "Aluguel de imóveis"
        ));

        createCategoryWithSubcategories("Receitas Financeiras", "bi-coins", "#2980b9", CategoryType.REVENUE, user, contaReceita, List.of(
                "Juros de Conta Corrente", "Previdência Privada", "Rendimentos de Criptomoedas"
        ));

        createCategoryWithSubcategories("Outras Receitas", "bi-gift", "#9b59b6", CategoryType.REVENUE, user, contaReceita, List.of(
                "Cashback", "Royalties", "Presentes"
        ));
    }

    private Account createContaPadrao(User user, String nomeConta) {
        Account account = new Account();
        account.setAccountName(nomeConta);
        account.setAccountDescription("Conta criada automaticamente para categorias padrão");
        account.setOpeningBalance(0.0);
        account.setCurrentBalance(0.0);
        account.setExpectedBalance(0.0);
        account.setSpecialCheck(0.0);
        account.setIncome(0.0);
        account.setExpense(0.0);
        account.setExpectedIncomeMonth(0.0);
        account.setExpectedExpenseMonth(0.0);
        account.setStatus(Status.SIM);
        account.setUser(user);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private void createCategoryWithSubcategories(String categoryName, String icon, String color,
                                                 CategoryType type, User user, Account account, List<String> subNames) {
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
        category.setAccount(account);

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
}