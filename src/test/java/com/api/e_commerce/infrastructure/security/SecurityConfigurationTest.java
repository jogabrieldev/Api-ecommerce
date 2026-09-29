package com.api.e_commerce.infrastructure.security;

import com.api.e_commerce.domain.security.AdministratorPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityTestEndpoints.class)
@Import({SecurityConfiguration.class, SecurityConfigurationTest.SecurityTestBeans.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("security-test")
class SecurityConfigurationTest {

    private final MockMvc mockMvc;
    private final JwtService jwtService;

    @Autowired
    SecurityConfigurationTest(MockMvc mockMvc, JwtService jwtService) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
    }

    @Test
    void shouldAllowCorsPreflightOnlyFromConfiguredSwaggerOrigin() throws Exception {
        mockMvc.perform(options("/administrators")
                        .header("Origin", "http://localhost:8080")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8080"));

        mockMvc.perform(options("/administrators")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowOnlyExplicitAnonymousEndpoints() throws Exception {
        mockMvc.perform(post("/customers"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/customers/authentication"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products/search"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/products/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/categories/00000000-0000-0000-0000-000000000001/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUnlistedEndpointToAnonymousUser() throws Exception {
        mockMvc.perform(get("/unlisted"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForAdministrativeEndpoints() throws Exception {
        mockMvc.perform(post("/administrators"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/administrators"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAdminToManageAdministratorsAndCategories() throws Exception {
        mockMvc.perform(post("/administrators").header("Authorization", basic("admin")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/administrators").header("Authorization", basic("admin")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/categories").header("Authorization", basic("admin")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRestrictManagerToCategoryManagement() throws Exception {
        mockMvc.perform(post("/administrators").header("Authorization", basic("manager")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/administrators").header("Authorization", basic("manager")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/categories").header("Authorization", basic("manager")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidCustomerFromAdministrativeEndpoints() throws Exception {
        mockMvc.perform(post("/administrators").header("Authorization", basic("customer")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/administrators").header("Authorization", basic("customer")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/categories").header("Authorization", basic("customer")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRestrictCustomerQueriesToAdmin() throws Exception {
        String byEmail = "/customers/email/customer@email.com";
        String analysis = byEmail + "/purchase-analysis";

        mockMvc.perform(get("/customers")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(byEmail)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(analysis)).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/customers").header("Authorization", basic("admin")))
                .andExpect(status().isOk());
        mockMvc.perform(get(byEmail).header("Authorization", basic("admin")))
                .andExpect(status().isOk());
        mockMvc.perform(get(analysis).header("Authorization", basic("admin")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/customers").header("Authorization", basic("manager")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(byEmail).header("Authorization", basic("customer")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(analysis).header("Authorization", basic("customer")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldApplyCatalogAndFinancialPermissionMatrix() throws Exception {
        String financial = "/administrators/00000000-0000-0000-0000-000000000001/financial-summary";

        for (String administrator : new String[]{"admin", "manager"}) {
            String authorization = basic(administrator);
            mockMvc.perform(post("/products").header("Authorization", authorization))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/products/import/fake-store")
                            .header("Authorization", authorization))
                    .andExpect(status().isOk());
            mockMvc.perform(get(financial).header("Authorization", authorization))
                    .andExpect(status().isOk());
        }

        String customer = basic("customer");
        mockMvc.perform(post("/products").header("Authorization", customer))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/products/import/fake-store").header("Authorization", customer))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(financial).header("Authorization", customer))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireBearerTokenForEveryCartOperation() throws Exception {
        String cart = "/customers/00000000-0000-0000-0000-000000000001/cart";

        mockMvc.perform(get(cart)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(cart + "/items")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch(cart + "/items/product-id")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete(cart + "/items/product-id")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete(cart)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(cart + "/checkout")).andExpect(status().isUnauthorized());

        mockMvc.perform(get(cart).header("Authorization", basic("customer")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowCustomerBearerTokenOnEveryCartOperation() throws Exception {
        when(jwtService.validateAndGetSubject("valid-token")).thenReturn("customer");
        String cart = "/customers/00000000-0000-0000-0000-000000000001/cart";
        String bearer = "Bearer valid-token";

        mockMvc.perform(get(cart).header("Authorization", bearer)).andExpect(status().isOk());
        mockMvc.perform(post(cart + "/items").header("Authorization", bearer)).andExpect(status().isOk());
        mockMvc.perform(patch(cart + "/items/product-id").header("Authorization", bearer)).andExpect(status().isOk());
        mockMvc.perform(delete(cart + "/items/product-id").header("Authorization", bearer)).andExpect(status().isOk());
        mockMvc.perform(delete(cart).header("Authorization", bearer)).andExpect(status().isOk());
        mockMvc.perform(post(cart + "/checkout").header("Authorization", bearer)).andExpect(status().isOk());
    }

    private static String basic(String username) {
        String credentials = username + ":password";
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SecurityTestBeans {

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            String password = passwordEncoder.encode("password");
            return username -> switch (username) {
                case "admin" -> User.withUsername(username)
                        .password(password)
                        .authorities(administratorAuthorities("ADMIN", true))
                        .build();
                case "manager" -> User.withUsername(username)
                        .password(password)
                        .authorities(administratorAuthorities("MANAGER", false))
                        .build();
                default -> User.withUsername(username)
                        .password(password)
                        .authorities("ROLE_CUSTOMER", "CUSTOMER_CHECKOUT")
                        .build();
            };
        }

        private static String[] administratorAuthorities(String role, boolean admin) {
            Stream<String> permissions = Stream.of(
                    AdministratorPermission.CATEGORY_MANAGE,
                    AdministratorPermission.PRODUCT_CREATE,
                    AdministratorPermission.PRODUCT_IMPORT,
                    AdministratorPermission.FINANCIAL_READ
            ).map(Enum::name);
            if (admin) {
                permissions = Stream.concat(permissions, Stream.of(
                        AdministratorPermission.ADMINISTRATOR_MANAGE.name(),
                        AdministratorPermission.CUSTOMER_READ.name()));
            }
            return Stream.concat(Stream.of("ROLE_" + role), permissions)
                    .toArray(String[]::new);
        }
    }
}

@RestController
@Profile("security-test")
class SecurityTestEndpoints {

    @PostMapping({
            "/customers",
            "/customers/authentication",
            "/administrators",
            "/categories",
            "/products",
            "/products/import/fake-store"
    })
    void postEndpoint() {
    }

    @GetMapping({
            "/products",
            "/products/search",
            "/products/{id}",
            "/categories",
            "/categories/{categoryId}/products",
            "/administrators",
            "/customers",
            "/customers/email/{email}",
            "/customers/email/{email}/purchase-analysis",
            "/unlisted"
    })
    void getEndpoint() {
    }

    @GetMapping("/administrators/{administratorId}/financial-summary")
    void getFinancialSummary() {
    }

    @GetMapping("/customers/{customerId}/cart")
    void getCart() {
    }

    @PostMapping({
            "/customers/{customerId}/cart/items",
            "/customers/{customerId}/cart/checkout"
    })
    void postCart() {
    }

    @org.springframework.web.bind.annotation.PatchMapping(
            "/customers/{customerId}/cart/items/{productId}")
    void patchCart() {
    }

    @org.springframework.web.bind.annotation.DeleteMapping({
            "/customers/{customerId}/cart",
            "/customers/{customerId}/cart/items/{productId}"
    })
    void deleteCart() {
    }
}
