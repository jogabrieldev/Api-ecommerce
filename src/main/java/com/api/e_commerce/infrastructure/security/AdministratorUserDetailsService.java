package com.api.e_commerce.infrastructure.security;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdministratorUserDetailsService implements UserDetailsService {

    private static final String CREATE_PRODUCT_PERMISSION = "PRODUCT_CREATE";

    private final AdministratorRepository administratorRepository;

    public AdministratorUserDetailsService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Administrator administrator = administratorRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Administrator not found"));

        return User.withUsername(administrator.getEmail())
                .password(administrator.getPasswordHash())
                .authorities(
                        "ROLE_" + administrator.getRole().name(),
                        CREATE_PRODUCT_PERMISSION
                )
                .disabled(!Boolean.TRUE.equals(administrator.getActive()))
                .build();
    }
}
