package com.api.e_commerce.infrastructure.config;

import com.api.e_commerce.presentation.rest.controller.CartController;
import com.api.e_commerce.presentation.rest.controller.CategoryController;
import com.api.e_commerce.presentation.rest.controller.CustomerController;
import com.api.e_commerce.presentation.rest.controller.ProductController;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Method;
import java.util.Set;

@Configuration
@Profile("demo")
public class DemoOpenApiConfig {

    private static final Set<String> CUSTOMER_METHODS = Set.of("create", "authenticate");
    private static final Set<String> PRODUCT_METHODS = Set.of("findAll", "findById", "search");
    private static final Set<String> CATEGORY_METHODS = Set.of("findAll", "findProducts");

    @Bean
    GroupedOpenApi demoCustomerFlowOpenApi() {
        return GroupedOpenApi.builder()
                .group("demo-customer-flow")
                .displayName("Fluxo do cliente")
                .pathsToMatch("/customers/**", "/products/**", "/categories/**")
                .addOpenApiMethodFilter(this::isDemoCustomerFlowMethod)
                .addOpenApiCustomizer(openApi -> {
                    openApi.setInfo(new Info()
                            .title("E-Commerce API - Demonstração")
                            .description("Fluxo público para cadastro, autenticação, catálogo, carrinho e checkout do cliente.")
                            .version("v1"));
                    if (openApi.getComponents() != null
                            && openApi.getComponents().getSecuritySchemes() != null) {
                        openApi.getComponents().getSecuritySchemes().remove("basicAuth");
                    }
                })
                .build();
    }

    private boolean isDemoCustomerFlowMethod(Method method) {
        Class<?> controller = method.getDeclaringClass();
        if (controller.equals(CartController.class)) {
            return true;
        }
        if (controller.equals(CustomerController.class)) {
            return CUSTOMER_METHODS.contains(method.getName());
        }
        if (controller.equals(CategoryController.class)) {
            return CATEGORY_METHODS.contains(method.getName());
        }
        return controller.equals(ProductController.class)
                && PRODUCT_METHODS.contains(method.getName());
    }
}
