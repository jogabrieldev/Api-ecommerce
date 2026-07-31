package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.repository.AdministratorFinancialRepository;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindAdministratorFinancialSummaryUseCaseTest {

    @Test
    void shouldReturnOwnFinancialSummary() {
        Administrator administrator = administrator();
        AdministratorFinancialRepository.Summary expected =
                new AdministratorFinancialRepository.Summary(
                        new BigDecimal("9000.00"), 2, 8);
        var useCase = new FindAdministratorFinancialSummaryUseCase(
                new AdministratorRepositoryStub(administrator),
                administratorId -> expected);

        var result = useCase.execute(1L, "admin@email.com");

        assertEquals(expected, result);
    }

    @Test
    void shouldRejectAnotherAdministrator() {
        Administrator administrator = administrator();
        var useCase = new FindAdministratorFinancialSummaryUseCase(
                new AdministratorRepositoryStub(administrator),
                administratorId -> new AdministratorFinancialRepository.Summary(
                        BigDecimal.ZERO, 0, 0));

        assertThrows(ForbiddenOperationException.class,
                () -> useCase.execute(1L, "other-admin@email.com"));
    }

    private static Administrator administrator() {
        Administrator administrator = new Administrator(
                "Administrator", "admin@email.com", "hash",
                "52998224725", AdministratorRole.ADMIN);
        try {
            Field id = Administrator.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(administrator, 1L);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        return administrator;
    }

    private record AdministratorRepositoryStub(Administrator administrator)
            implements AdministratorRepository {
        @Override
        public Administrator save(Administrator administrator) {
            return administrator;
        }

        @Override
        public List<Administrator> getUserAdm() {
            return List.of(administrator);
        }

        @Override
        public Optional<Administrator> findById(Long id) {
            return administrator.getId().equals(id)
                    ? Optional.of(administrator)
                    : Optional.empty();
        }

        @Override
        public Optional<Administrator> findByEmail(String email) {
            return administrator.getEmail().equals(email)
                    ? Optional.of(administrator)
                    : Optional.empty();
        }

        @Override
        public boolean existsByEmail(String email) {
            return administrator.getEmail().equals(email);
        }

        @Override
        public boolean existsByCpf(String cpf) {
            return administrator.getCpf().equals(cpf);
        }
    }
}
