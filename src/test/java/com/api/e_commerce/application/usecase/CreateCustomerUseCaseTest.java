package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateCustomerUseCaseTest {

    private InMemoryCustomerRepository repository;
    private CreateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
        useCase = new CreateCustomerUseCase(repository, password -> "hashed:" + password);
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
        public Optional<Customer> findByIdForUpdate(Long id) {
            return Optional.ofNullable(customer);
        }
    }
}
