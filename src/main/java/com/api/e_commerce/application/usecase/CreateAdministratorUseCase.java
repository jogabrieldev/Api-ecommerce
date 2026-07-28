package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.security.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class CreateAdministratorUseCase {

    private final AdministratorRepository administratorRepository;
    private final PasswordHasher passwordHasher;

    public CreateAdministratorUseCase(
            AdministratorRepository administratorRepository,
            PasswordHasher passwordHasher
    ) {
        this.administratorRepository = administratorRepository;
        this.passwordHasher = passwordHasher;
    }

    public Administrator execute(String name, String email, String password, String cpf,
                                 AdministratorRole role) {
        String normalizedEmail = email.trim().toLowerCase();

        if (administratorRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("E-mail already registered");
        }
        if (administratorRepository.existsByCpf(cpf)) {
            throw new ConflictException("CPF already registered");
        }

        String passwordHash = passwordHasher.hash(password);
        Administrator administrator =
                new Administrator(name.trim(), normalizedEmail, passwordHash, cpf, role);
        return administratorRepository.save(administrator);
    }
}
