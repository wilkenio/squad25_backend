package com.financeiro.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Squad 25")
                        .version("1.0")
                        .description("Documentação do Sistema Financeiro do Squad 25"))
                .addServersItem(new Server().url("http://localhost:8080").description("Servidor Local"))
                .addServersItem(new Server().url("https://apisquad25.fourdevs.com.br").description("Servidor de Produção"));
    }
}
