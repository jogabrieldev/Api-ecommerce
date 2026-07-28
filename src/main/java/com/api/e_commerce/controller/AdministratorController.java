package com.api.e_commerce.controller;

import com.api.e_commerce.model.Administrator;
import com.api.e_commerce.model.AdministratorRole;
import com.api.e_commerce.service.AdministratorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/administrators")
public class AdministratorController {

    private final AdministratorService administratorService;

    public AdministratorController(AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @PostMapping
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateRequest request) {
        Administrator administrator = administratorService.create(
                request.name(),
                request.email(),
                request.passwordHash(),
                request.cpf(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(administrator.getId()));
    }

    public record CreateRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 255) String passwordHash,
            @NotBlank
            @Pattern(regexp = "\\d{11}", message = "CPF must contain exactly 11 digits")
            String cpf,
            @NotNull AdministratorRole role
    ) {
    }

    public record CreatedResponse(Long id) {
    }
}
