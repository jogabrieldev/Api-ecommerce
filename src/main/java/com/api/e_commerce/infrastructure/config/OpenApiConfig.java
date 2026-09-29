package com.api.e_commerce.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI eCommerceOpenApi() {
        return new OpenAPI()
                .info(new Info().title("E-Commerce API")
                        .description("API REST para gerenciamento de e-commerce desenvolvida com Java e Spring Boot. "
                                + "Clientes usam JWT Bearer no checkout; administradores usam HTTP Basic.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}
