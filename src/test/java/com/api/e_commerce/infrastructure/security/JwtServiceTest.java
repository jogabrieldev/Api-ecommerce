package com.api.e_commerce.infrastructure.security;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService service = new JwtService(
                new ObjectMapper(),
                "ecommerce-local-jwt-secret-change-in-production-2026",
                3600
        );

        JwtService.Token token = service.generate("customer@email.com");

        assertEquals("customer@email.com",
                service.validateAndGetSubject(token.value()));
    }
}
