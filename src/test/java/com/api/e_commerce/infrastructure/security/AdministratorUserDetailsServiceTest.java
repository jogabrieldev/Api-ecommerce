package com.api.e_commerce.infrastructure.security;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.security.AdministratorPermission;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdministratorUserDetailsServiceTest {

    @Test
    void shouldLoadAdminWithEveryAdministrativePermission() {
        Set<String> authorities = loadAuthorities(AdministratorRole.ADMIN);

        assertEquals(Set.of(
                "ROLE_ADMIN",
                AdministratorPermission.ADMINISTRATOR_MANAGE.name(),
                AdministratorPermission.CUSTOMER_READ.name(),
                AdministratorPermission.CATEGORY_MANAGE.name(),
                AdministratorPermission.PRODUCT_CREATE.name(),
                AdministratorPermission.PRODUCT_IMPORT.name(),
                AdministratorPermission.FINANCIAL_READ.name()
        ), authorities);
    }

    @Test
    void shouldLoadManagerWithoutSensitiveManagementPermissions() {
        Set<String> authorities = loadAuthorities(AdministratorRole.MANAGER);

        assertEquals(Set.of(
                "ROLE_MANAGER",
                AdministratorPermission.CATEGORY_MANAGE.name(),
                AdministratorPermission.PRODUCT_CREATE.name(),
                AdministratorPermission.PRODUCT_IMPORT.name(),
                AdministratorPermission.FINANCIAL_READ.name()
        ), authorities);
    }

    private static Set<String> loadAuthorities(AdministratorRole role) {
        AdministratorRepository administratorRepository = mock(AdministratorRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        Administrator administrator = new Administrator(
                "Administrator", "admin@email.com", "hash",
                "52998224725", role);
        when(administratorRepository.findByEmail("admin@email.com"))
                .thenReturn(Optional.of(administrator));

        var service = new AdministratorUserDetailsService(
                administratorRepository, customerRepository);
        return service.loadUserByUsername("admin@email.com")
                .getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
    }
}
