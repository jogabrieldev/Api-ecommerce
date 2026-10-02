package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.CustomerAddress;
import com.api.e_commerce.domain.model.IdentityType;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.UserIdentityRegistry;
import com.api.e_commerce.domain.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final UserIdentityRegistry userIdentityRegistry;

    public CreateCustomerUseCase(CustomerRepository customerRepository,
                                 PasswordHasher passwordHasher,
                                 UserIdentityRegistry userIdentityRegistry) {
        this.customerRepository = customerRepository;
        this.passwordHasher = passwordHasher;
        this.userIdentityRegistry = userIdentityRegistry;
    }

    @Transactional
    public Customer execute(String name, String email, String password, String cpf, String phone,
                            LocalDate birthDate, AddressData addressData) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedCpf = digitsOnly(cpf);
        String normalizedPhone = digitsOnly(phone);

        if (birthDate.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Birth date cannot be in the future");
        }
        String passwordHash = passwordHasher.hash(password);
        userIdentityRegistry.claim(normalizedEmail, normalizedCpf, IdentityType.CUSTOMER);

        CustomerAddress address = new CustomerAddress(
                digitsOnly(addressData.zipCode()),
                addressData.street().trim(),
                addressData.number().trim(),
                normalizeOptional(addressData.complement()),
                addressData.neighborhood().trim(),
                addressData.city().trim(),
                addressData.state().trim().toUpperCase(Locale.ROOT)
        );
        Customer customer = new Customer(
                name.trim(),
                normalizedEmail,
                passwordHash,
                normalizedCpf,
                normalizedPhone,
                birthDate,
                address
        );
        return customerRepository.save(customer);
    }

    private static String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record AddressData(
            String zipCode,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state
    ) {
    }
}
