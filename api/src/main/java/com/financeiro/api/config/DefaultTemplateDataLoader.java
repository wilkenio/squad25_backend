package com.financeiro.api.config;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.template.CategoryTemplate;
import com.financeiro.api.domain.template.SubcategoryTemplate;
import com.financeiro.api.repository.template.CategoryTemplateRepository;
import com.financeiro.api.repository.template.SubcategoryTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DefaultTemplateDataLoader {

    private final CategoryTemplateRepository categoryTemplateRepository;
    private final SubcategoryTemplateRepository subcategoryTemplateRepository;

    public DefaultTemplateDataLoader(CategoryTemplateRepository categoryTemplateRepository,
                                     SubcategoryTemplateRepository subcategoryTemplateRepository) {
        this.categoryTemplateRepository = categoryTemplateRepository;
        this.subcategoryTemplateRepository = subcategoryTemplateRepository;
    }

    @PostConstruct
    public void load() {
        if (categoryTemplateRepository.count() == 0) {
            System.out.println("🚀 Carregando categorias e subcategorias padrão...");

            // === EXPENSE ===

            // Casa
            CategoryTemplate casa = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Casa")
                    .color("#3498db")
                    .iconClass("bi-house")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Água").color("#5dade2").iconClass("bi-droplet").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Energia elétrica").color("#f39c12").iconClass("bi-lightning-charge").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Gás").color("#e67e22").iconClass("bi-fire").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Internet").color("#2ecc71").iconClass("bi-wifi").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Condomínio").color("#9b59b6").iconClass("bi-building").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Aluguel").color("#c0392b").iconClass("bi-key").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Manutenção").color("#7f8c8d").iconClass("bi-wrench").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("IPTU").color("#16a085").iconClass("bi-receipt").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Seguro residencial").color("#34495e").iconClass("bi-shield-lock").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Alimentação
            CategoryTemplate alimentacao = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Alimentação")
                    .color("#e74c3c")
                    .iconClass("bi-utensils")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Supermercado").color("#27ae60").iconClass("bi-basket3").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Feira").color("#2ecc71").iconClass("bi-leaf").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Restaurantes").color("#d35400").iconClass("bi-shop-window").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Delivery").color("#f1c40f").iconClass("bi-truck").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Lanches").color("#e67e22").iconClass("bi-basket").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Transporte
            CategoryTemplate transporte = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Transporte")
                    .color("#3498db")
                    .iconClass("bi-car-front")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Combustível").color("#e74c3c").iconClass("bi-fuel-pump").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Transporte público").color("#8e44ad").iconClass("bi-bus-front").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Aplicativos").color("#1abc9c").iconClass("bi-taxi-front").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Estacionamento").color("#34495e").iconClass("bi-parking").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Pedágio").color("#f39c12").iconClass("bi-signpost-split").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Manutenção do veículo").color("#7f8c8d").iconClass("bi-tools").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Seguro do veículo").color("#2c3e50").iconClass("bi-shield-car").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("IPVA").color("#16a085").iconClass("bi-receipt").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Financiamento do carro").color("#2980b9").iconClass("bi-hand-holding-dollar").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Contas e Assinaturas
            CategoryTemplate contas = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Contas e Assinaturas")
                    .color("#9b59b6")
                    .iconClass("bi-receipt")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Celular").color("#3498db").iconClass("bi-phone").status(Status.SIM).categoryTemplate(contas).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Streaming").color("#e74c3c").iconClass("bi-play-circle").status(Status.SIM).categoryTemplate(contas).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // === REVENUE ===
            // Contas
            CategoryTemplate conta = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Contas")
                    .color("#2ecc71")
                    .iconClass("bi-wallet")
                    .type(CategoryType.REVENUE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("NuBank").color("#27ae60").iconClass("bi-currency-dollar").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Caixa").color("#2ecc71").iconClass("bi-gift").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Banco do Brasil").color("#16a085").iconClass("bi-graph-up").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Bradesco").color("#1abc9c").iconClass("bi-calendar-check").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Itaú").color("#f39c12").iconClass("bi-egg-fried").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Santander").color("#e67e22").iconClass("bi-receipt-cutoff").status(Status.SIM).categoryTemplate(conta).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Salário e Benefícios
            CategoryTemplate salario = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Salário e Benefícios")
                    .color("#2ecc71")
                    .iconClass("bi-wallet")
                    .type(CategoryType.REVENUE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Salário Base").color("#27ae60").iconClass("bi-currency-dollar").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Bônus").color("#2ecc71").iconClass("bi-gift").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Comissões").color("#16a085").iconClass("bi-graph-up").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("13º Salário").color("#1abc9c").iconClass("bi-calendar-check").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Vale-refeição").color("#f39c12").iconClass("bi-egg-fried").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Reembolso de despesas").color("#e67e22").iconClass("bi-receipt-cutoff").status(Status.SIM).categoryTemplate(salario).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Investimentos
            CategoryTemplate investimentos = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Investimentos")
                    .color("#27ae60")
                    .iconClass("bi-pie-chart")
                    .type(CategoryType.REVENUE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Dividendos").color("#2ecc71").iconClass("bi-piggy-bank").status(Status.SIM).categoryTemplate(investimentos).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Juros de Poupança").color("#16a085").iconClass("bi-piggy-bank-fill").status(Status.SIM).categoryTemplate(investimentos).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Juros de CDB").color("#1abc9c").iconClass("bi-bar-chart").status(Status.SIM).categoryTemplate(investimentos).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Ganho de Capital").color("#f1c40f").iconClass("bi-graph-up-arrow").status(Status.SIM).categoryTemplate(investimentos).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Aluguel de imóveis").color("#9b59b6").iconClass("bi-building").status(Status.SIM).categoryTemplate(investimentos).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Receitas Financeiras
            CategoryTemplate financRel = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Receitas Financeiras")
                    .color("#2980b9")
                    .iconClass("bi-coins")
                    .type(CategoryType.REVENUE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Juros de Conta Corrente").color("#3498db").iconClass("bi-bank").status(Status.SIM).categoryTemplate(financRel).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Previdência Privada").color("#2ecc71").iconClass("bi-shield-shaded").status(Status.SIM).categoryTemplate(financRel).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Rendimentos de Criptomoedas").color("#f1c40f").iconClass("bi-currency-bitcoin").status(Status.SIM).categoryTemplate(financRel).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            // Outras Receitas
            CategoryTemplate outras = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Outras Receitas")
                    .color("#9b59b6")
                    .iconClass("bi-gift")
                    .type(CategoryType.REVENUE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Cashback").color("#27ae60").iconClass("bi-wallet2").status(Status.SIM).categoryTemplate(outras).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Royalties").color("#2ecc71").iconClass("bi-music-note-list").status(Status.SIM).categoryTemplate(outras).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Presentes").color("#f58220").iconClass("bi-gift-fill").status(Status.SIM).categoryTemplate(outras).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            System.out.println("✅ Templates de categorias carregados com sucesso.");
        }
    }
}
