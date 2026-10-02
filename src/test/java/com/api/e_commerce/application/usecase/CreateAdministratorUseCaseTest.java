package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.model.IdentityType;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.UserIdentityRegistry;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateAdministratorUseCaseTest {

    @Test
    void managerShouldClaimNormalizedIdentityBeforeCreatingAdmin() {
        RecordingIdentityRegistry identityRegistry = new RecordingIdentityRegistry();
        AdministratorRepository repository = mock(AdministratorRepository.class);
        Administrator manager = administrator("Manager", "manager@email.com", AdministratorRole.MANAGER);
        when(repository.findByEmail("manager@email.com")).thenReturn(Optional.of(manager));
        when(repository.save(any(Administrator.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAdministratorUseCase useCase = new CreateAdministratorUseCase(
                repository,
                password -> "hashed:" + password,
                identityRegistry);

        Administrator administrator = useCase.execute(
                "Administrator", " ADMIN@EMAIL.COM ", "secure-password",
                "52998224725", AdministratorRole.ADMIN, " MANAGER@EMAIL.COM ");

        assertEquals("admin@email.com", administrator.getEmail());
        assertEquals(AdministratorRole.ADMIN, administrator.getRole());
        assertEquals("admin@email.com", identityRegistry.email);
        assertEquals("52998224725", identityRegistry.cpf);
        assertEquals(IdentityType.ADMINISTRATOR, identityRegistry.type);
    }

    @Test
    void adminShouldNotCreateAnotherAdministrator() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        when(repository.findByEmail("admin@email.com")).thenReturn(Optional.of(
                administrator("Admin", "admin@email.com", AdministratorRole.ADMIN)));
        CreateAdministratorUseCase useCase = useCase(repository, new RecordingIdentityRegistry());

        assertThrows(ForbiddenOperationException.class, () -> useCase.execute(
                "Another Admin", "other@email.com", "secure-password",
                "52998224725", AdministratorRole.ADMIN, "admin@email.com"));
    }

    @Test
    void managerShouldNotCreateAnotherManagerThroughTheApi() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        when(repository.findByEmail("manager@email.com")).thenReturn(Optional.of(
                administrator("Manager", "manager@email.com", AdministratorRole.MANAGER)));
        CreateAdministratorUseCase useCase = useCase(repository, new RecordingIdentityRegistry());

        assertThrows(ForbiddenOperationException.class, () -> useCase.execute(
                "Another Manager", "other@email.com", "secure-password",
                "52998224725", AdministratorRole.MANAGER, "manager@email.com"));
    }

    @Test
    void shouldCreateInitialManagerOnlyWhenThereAreNoAdministrators() {
        AdministratorRepository repository = mock(AdministratorRepository.class);
        when(repository.save(any(Administrator.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAdministratorUseCase useCase = useCase(repository, new RecordingIdentityRegistry());

        Administrator manager = useCase.createInitialManager(
                "Initial Manager", "manager@email.com", "secure-password", "52998224725");

        assertEquals(AdministratorRole.MANAGER, manager.getRole());

        when(repository.existsAny()).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> useCase.createInitialManager(
                "Another Manager", "other@email.com", "secure-password", "11144477735"));
    }

    private static CreateAdministratorUseCase useCase(
            AdministratorRepository repository, UserIdentityRegistry identityRegistry) {
        return new CreateAdministratorUseCase(
                repository, password -> "hashed:" + password, identityRegistry);
    }

    private static Administrator administrator(
            String name, String email, AdministratorRole role) {
        return new Administrator(name, email, "hashed-password", "11144477735", role);
    }

    private static class RecordingIdentityRegistry implements UserIdentityRegistry {
        private String email;
        private String cpf;
        private IdentityType type;

        @Override
        public void claim(String email, String cpf, IdentityType type) {
            this.email = email;
            this.cpf = cpf;
            this.type = type;
        }
    }

}
