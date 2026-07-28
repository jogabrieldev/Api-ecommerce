package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindAllUserAdmUseCase {

    private final AdministratorRepository administratorRepository;

    public FindAllUserAdmUseCase(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    public List<Administrator> execute() {
        return administratorRepository.getUserAdm();
    }
}
