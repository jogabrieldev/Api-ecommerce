package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.model.IdentityType;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.UserIdentityRegistry;
import com.api.e_commerce.domain.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CreateAdministratorUseCase {

    private final AdministratorRepository administratorRepository;
    private final PasswordHasher passwordHasher;
    private final UserIdentityRegistry userIdentityRegistry;

    public CreateAdministratorUseCase(AdministratorRepository administratorRepository,
                                      PasswordHasher passwordHasher,
                                      UserIdentityRegistry userIdentityRegistry) {
        this.administratorRepository = administratorRepository;
        this.passwordHasher = passwordHasher;
        this.userIdentityRegistry = userIdentityRegistry;
    }

    @Transactional
    public Administrator execute(String name, String email, String password, String cpf,
                                 AdministratorRole role, String authenticatedEmail) {
        administratorRepository
                .findByEmail(authenticatedEmail.trim().toLowerCase(Locale.ROOT))
                .filter(administrator -> Boolean.TRUE.equals(administrator.getActive()))
                .filter(administrator -> administrator.getRole() == AdministratorRole.MANAGER)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Only an active manager can create administrators"));

        if (role != AdministratorRole.ADMIN) {
            throw new ForbiddenOperationException(
                    "Managers can only create administrators with the ADMIN role");
        }

        return create(name, email, password, cpf, role);
    }

    @Transactional
    public Administrator createInitialManager(String name, String email, String password, String cpf) {
        administratorRepository.lockInitialManagerCreation();
        if (administratorRepository.existsAny()) {
            throw new BusinessRuleException("The initial manager can only be created in an empty installation");
        }
        return create(name, email, password, cpf, AdministratorRole.MANAGER);
    }

    private Administrator create(String name, String email, String password, String cpf,
                                 AdministratorRole role) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String passwordHash = passwordHasher.hash(password);

        userIdentityRegistry.claim(normalizedEmail, cpf, IdentityType.ADMINISTRATOR);

        Administrator administrator =
                new Administrator(name.trim(), normalizedEmail, passwordHash, cpf, role);
        return administratorRepository.save(administrator);
    }
}
