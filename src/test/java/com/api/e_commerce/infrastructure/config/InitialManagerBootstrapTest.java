package com.api.e_commerce.infrastructure.config;

import com.api.e_commerce.application.usecase.CreateAdministratorUseCase;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitialManagerBootstrapTest {

    @Test
    void shouldCreateOnlyTheFirstAdministrativeUserAsManager() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        CreateAdministratorUseCase createUseCase = mock(CreateAdministratorUseCase.class);
        when(repository.existsAny()).thenReturn(false);
        when(createUseCase.createInitialManager(
                "Initial Manager", "manager@email.com", "secure-password",
                "52998224725"))
                .thenReturn(mock(Administrator.class));

        bootstrap(repository, createUseCase).run(null);

        verify(createUseCase).createInitialManager(
                "Initial Manager", "manager@email.com", "secure-password",
                "52998224725");
    }

    @Test
    void shouldDoNothingWhenAnAdministratorAlreadyExists() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        CreateAdministratorUseCase createUseCase = mock(CreateAdministratorUseCase.class);
        when(repository.existsAny()).thenReturn(true);

        bootstrap(repository, createUseCase).run(null);

        verify(createUseCase, never()).createInitialManager(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectWeakBootstrapPassword() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        CreateAdministratorUseCase createUseCase = mock(CreateAdministratorUseCase.class);
        when(repository.existsAny()).thenReturn(false);
        InitialManagerBootstrap bootstrap = new InitialManagerBootstrap(
                repository, createUseCase, "Initial Manager", "manager@email.com",
                "short", "52998224725");

        assertThrows(IllegalStateException.class, () -> bootstrap.run(null));
        verify(createUseCase, never()).createInitialManager(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private InitialManagerBootstrap bootstrap(
            AdministratorRepository repository, CreateAdministratorUseCase createUseCase) {
        return new InitialManagerBootstrap(
                repository, createUseCase, " Initial Manager ", " MANAGER@EMAIL.COM ",
                "secure-password", "529.982.247-25");
    }
}
