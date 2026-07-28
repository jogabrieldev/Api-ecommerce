package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateAdministratorUseCase;
import com.api.e_commerce.application.usecase.FindAllUserAdmUseCase;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.presentation.rest.request.CreateAdministratorRequest;
import com.api.e_commerce.presentation.rest.response.AdministratorResponse;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/administrators")
public class AdministratorController {

    private final CreateAdministratorUseCase createAdministratorUseCase;
    private final FindAllUserAdmUseCase findAllUserAdmUseCase;

    public AdministratorController(
            CreateAdministratorUseCase createAdministratorUseCase,
            FindAllUserAdmUseCase findAllUserAdmUseCase
    ) {
        this.createAdministratorUseCase = createAdministratorUseCase;
        this.findAllUserAdmUseCase = findAllUserAdmUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateAdministratorRequest request) {
        Administrator administrator = createAdministratorUseCase.execute(
                request.name(),
                request.email(),
                request.password(),
                request.cpf(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(administrator.getId()));
    }

    @GetMapping
    public ResponseEntity<List<AdministratorResponse>> findAll() {
        List<AdministratorResponse> administrators = findAllUserAdmUseCase.execute()
                .stream()
                .map(AdministratorResponse::from)
                .toList();
        return ResponseEntity.ok(administrators);
    }
}
