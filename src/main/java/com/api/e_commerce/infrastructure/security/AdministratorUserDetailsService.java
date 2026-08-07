package com.api.e_commerce.infrastructure.security;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdministratorUserDetailsService implements UserDetailsService {

    private static final String CREATE_PRODUCT_PERMISSION = "PRODUCT_CREATE";
    private static final String CHECKOUT_PERMISSION = "CUSTOMER_CHECKOUT";
    private static final String FINANCIAL_READ_PERMISSION = "FINANCIAL_READ";
    private static final String IMPORT_PRODUCT_PERMISSION = "PRODUCT_IMPORT";

    private final AdministratorRepository administratorRepository;
    private final CustomerRepository customerRepository;

    public AdministratorUserDetailsService(AdministratorRepository administratorRepository,
                                           CustomerRepository customerRepository) {
        this.administratorRepository = administratorRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.trim().toLowerCase();
        var administrator = administratorRepository.findByEmail(normalizedEmail);

        if (administrator.isPresent()) {
            Administrator user = administrator.get();
            return User.withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(
                            "ROLE_" + user.getRole().name(),
                            CREATE_PRODUCT_PERMISSION,
                            IMPORT_PRODUCT_PERMISSION,
                            FINANCIAL_READ_PERMISSION)
                    .disabled(!Boolean.TRUE.equals(user.getActive()))
                    .build();
        }

        return customerRepository.findByEmail(normalizedEmail)
                .map(customer -> User.withUsername(customer.getEmail())
                        .password(customer.getPasswordHash())
                        .authorities("ROLE_CUSTOMER", CHECKOUT_PERMISSION)
                        .disabled(!Boolean.TRUE.equals(customer.getActive()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
