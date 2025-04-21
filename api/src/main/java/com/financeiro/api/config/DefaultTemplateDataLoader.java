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

            CategoryTemplate casa = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Casa")
                    .color("#3498db")
                    .iconClass("fa-home")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Água").color("#5dade2").iconClass("fa-tint").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Energia elétrica").color("#f39c12").iconClass("fa-bolt").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Gás").color("#e67e22").iconClass("fa-fire").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Internet").color("#2ecc71").iconClass("fa-wifi").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Condomínio").color("#9b59b6").iconClass("fa-building").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Aluguel").color("#c0392b").iconClass("fa-key").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Manutenção").color("#7f8c8d").iconClass("fa-wrench").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("IPTU").color("#16a085").iconClass("fa-file-invoice-dollar").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Seguro residencial").color("#34495e").iconClass("fa-shield-alt").status(Status.SIM).categoryTemplate(casa).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            CategoryTemplate alimentacao = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Alimentação")
                    .color("#e74c3c")
                    .iconClass("fa-utensils")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Supermercado").color("#27ae60").iconClass("fa-shopping-basket").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Feira").color("#2ecc71").iconClass("fa-leaf").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Restaurantes").color("#d35400").iconClass("fa-concierge-bell").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Delivery").color("#f1c40f").iconClass("fa-motorcycle").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Lanches").color("#e67e22").iconClass("fa-hamburger").status(Status.SIM).categoryTemplate(alimentacao).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            CategoryTemplate transporte = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Transporte")
                    .color("#3498db")
                    .iconClass("fa-car")
                    .type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Combustível").color("#e74c3c").iconClass("fa-gas-pump").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Transporte público").color("#8e44ad").iconClass("fa-bus").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Aplicativos").color("#1abc9c").iconClass("fa-taxi").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Estacionamento").color("#34495e").iconClass("fa-parking").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Pedágio").color("#f39c12").iconClass("fa-road").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Manutenção do veículo").color("#7f8c8d").iconClass("fa-tools").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Seguro do veículo").color("#2c3e50").iconClass("fa-shield-car").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("IPVA").color("#16a085").iconClass("fa-file-invoice").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Financiamento do carro").color("#2980b9").iconClass("fa-hand-holding-usd").status(Status.SIM).categoryTemplate(transporte).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            CategoryTemplate contas = categoryTemplateRepository.save(
                CategoryTemplate.builder()
                    .name("Contas e Assinaturas")
                    .color("#9b59b6")
                    .iconClass("fa-file-invoice").type(CategoryType.EXPENSE)
                    .status(Status.SIM)
                    .standardRecommendation(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                .build()
            );
            subcategoryTemplateRepository.saveAll(List.of(
                SubcategoryTemplate.builder().name("Celular").color("#3498db").iconClass("fa-mobile-alt").status(Status.SIM).categoryTemplate(contas).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                SubcategoryTemplate.builder().name("Streaming").color("#e74c3c").iconClass("fa-play-circle").status(Status.SIM).categoryTemplate(contas).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            ));

            System.out.println("✅ Templates de categorias carregados com sucesso.");
        }
    }
}
