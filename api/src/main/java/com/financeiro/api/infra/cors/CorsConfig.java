package com.financeiro.api.infra.cors;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200") //endereço padrão do angular
                .allowedMethods("GET", "POST"); //outros métodos serão aceitos posteriormente
    }
}
