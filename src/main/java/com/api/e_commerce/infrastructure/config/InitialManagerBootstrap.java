package com.api.e_commerce.infrastructure.config;

import com.api.e_commerce.application.usecase.CreateAdministratorUseCase;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.validation.CpfValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(
        prefix = "bootstrap.initial-manager",
        name = "enabled",
        havingValue = "true")
public class InitialManagerBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialManagerBootstrap.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AdministratorRepository administratorRepository;
    private final CreateAdministratorUseCase createAdministratorUseCase;
    private final String name;
    private final String email;
    private final String password;
    private final String cpf;

    public InitialManagerBootstrap(
            AdministratorRepository administratorRepository,
            CreateAdministratorUseCase createAdministratorUseCase,
            @Value("${bootstrap.initial-manager.name:}") String name,
            @Value("${bootstrap.initial-manager.email:}") String email,
            @Value("${bootstrap.initial-manager.password:}") String password,
            @Value("${bootstrap.initial-manager.cpf:}") String cpf) {
        this.administratorRepository = administratorRepository;
        this.createAdministratorUseCase = createAdministratorUseCase;
        this.name = name;
        this.email = email;
        this.password = password;
        this.cpf = cpf;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (administratorRepository.existsAny()) {
            LOGGER.info("Initial manager bootstrap skipped because an administrator already exists");
            return;
        }

        BootstrapData data = validateAndNormalize();
        Administrator manager = createAdministratorUseCase.createInitialManager(
                data.name(), data.email(), data.password(), data.cpf());
        LOGGER.info("Initial manager created successfully with id {}", manager.getId());
    }

    private BootstrapData validateAndNormalize() {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedCpf = cpf == null ? "" : cpf.replaceAll("\\D", "");

        if (normalizedName.isBlank() || normalizedName.length() > 150) {
            throw invalidConfiguration("name must contain between 1 and 150 characters");
        }
        if (normalizedEmail.length() > 150 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw invalidConfiguration("email must be valid and contain at most 150 characters");
        }
        if (password == null || password.length() < 12 || password.length() > 72) {
            throw invalidConfiguration("password must contain between 12 and 72 characters");
        }
        if (normalizedCpf.length() != 11 || !new CpfValidator().isValid(normalizedCpf, null)) {
            throw invalidConfiguration("cpf must be valid");
        }
        return new BootstrapData(normalizedName, normalizedEmail, password, normalizedCpf);
    }

    private IllegalStateException invalidConfiguration(String detail) {
        return new IllegalStateException("Invalid initial manager bootstrap configuration: " + detail);
    }

    private record BootstrapData(String name, String email, String password, String cpf) {
    }
}
