package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.IdentityType;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.UserIdentityRegistry;
import com.api.e_commerce.domain.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateCustomerUseCaseTest {

    private InMemoryCustomerRepository repository;
    private InMemoryUserIdentityRegistry identityRegistry;
    private CreateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
        identityRegistry = new InMemoryUserIdentityRegistry();
        useCase = new CreateCustomerUseCase(
                repository, password -> "hashed:" + password, identityRegistry);
    }

    @Test
    void shouldNormalizeAndCreateCustomerWithAddress() {
        Customer customer = execute(" CUSTOMER@EMAIL.COM ", "529.982.247-25");

        assertEquals("customer@email.com", customer.getEmail());
        assertEquals("52998224725", customer.getCpf());
        assertEquals("11999998888", customer.getPhone());
        assertEquals("01310100", customer.getAddress().getZipCode());
        assertEquals("SP", customer.getAddress().getState());
        assertNotEquals("secure-password", customer.getPasswordHash());
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        execute("customer@email.com", "52998224725");

        assertThrows(ConflictException.class,
                () -> execute(" CUSTOMER@EMAIL.COM ", "11144477735"));
    }

    @Test
    void shouldClaimNormalizedEmailAndCpfInTheGlobalIdentityRegistry() {
        execute(" CUSTOMER@EMAIL.COM ", "529.982.247-25");

        assertEquals(Set.of("customer@email.com"), identityRegistry.emails);
        assertEquals(Set.of("52998224725"), identityRegistry.cpfs);
        assertEquals(IdentityType.CUSTOMER, identityRegistry.lastType);
    }

    private Customer execute(String email, String cpf) {
        return useCase.execute(
                "Customer Name",
                email,
                "secure-password",
                cpf,
                "(11) 99999-8888",
                LocalDate.of(1990, 1, 1),
                new CreateCustomerUseCase.AddressData(
                        "01310-100",
                        "Avenida Paulista",
                        "1000",
                        "Apartment 10",
                        "Bela Vista",
                        "Sao Paulo",
                        "sp"
                )
        );
    }

    private static class InMemoryCustomerRepository implements CustomerRepository {

        private Customer customer;

        @Override
        public Customer save(Customer customer) {
            this.customer = customer;
            return customer;
        }

        @Override
        public boolean existsByEmail(String email) {
            return customer != null && customer.getEmail().equals(email);
        }

        @Override
        public boolean existsByCpf(String cpf) {
            return customer != null && customer.getCpf().equals(cpf);
        }

        @Override
        public Optional<Customer> findByEmail(String email) {
            return customer != null && customer.getEmail().equals(email)
                    ? Optional.of(customer)
                    : Optional.empty();
        }

        @Override
        public Optional<Customer> findByIdForUpdate(java.util.UUID id) {
            return Optional.ofNullable(customer);
        }
    }

    private static class InMemoryUserIdentityRegistry implements UserIdentityRegistry {
        private final Set<String> emails = new HashSet<>();
        private final Set<String> cpfs = new HashSet<>();
        private IdentityType lastType;

        @Override
        public void claim(String email, String cpf, IdentityType type) {
            if (!emails.add(email)) {
                throw new ConflictException("E-mail already registered");
            }
            if (!cpfs.add(cpf)) {
                emails.remove(email);
                throw new ConflictException("CPF already registered");
            }
            lastType = type;
        }
    }
}
