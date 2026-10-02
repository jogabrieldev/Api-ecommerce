package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateAdministratorUseCase;
import com.api.e_commerce.application.usecase.FindAllUserAdmUseCase;
import com.api.e_commerce.application.usecase.FindAdministratorFinancialSummaryUseCase;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.presentation.rest.request.CreateAdministratorRequest;
import com.api.e_commerce.presentation.rest.response.AdministratorResponse;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import com.api.e_commerce.presentation.rest.response.AdministratorFinancialSummaryResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/administrators")
@Validated
@Tag(name = "Administradores", description = "Cadastro, consulta e resumo financeiro")
public class AdministratorController {

    private final CreateAdministratorUseCase createAdministratorUseCase;
    private final FindAllUserAdmUseCase findAllUserAdmUseCase;
    private final FindAdministratorFinancialSummaryUseCase financialSummaryUseCase;

    public AdministratorController(
            CreateAdministratorUseCase createAdministratorUseCase,
            FindAllUserAdmUseCase findAllUserAdmUseCase,
            FindAdministratorFinancialSummaryUseCase financialSummaryUseCase
    ) {
        this.createAdministratorUseCase = createAdministratorUseCase;
        this.findAllUserAdmUseCase = findAllUserAdmUseCase;
        this.financialSummaryUseCase = financialSummaryUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar administrador",
            description = "Operação exclusiva de administradores com papel MANAGER. "
                    + "A API permite criar somente usuários com papel ADMIN.",
            security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<CreatedResponse> create(
            @Valid @RequestBody CreateAdministratorRequest request,
            Authentication authentication) {
        Administrator administrator = createAdministratorUseCase.execute(
                request.name(),
                request.email(),
                request.password(),
                request.cpf(),
                request.role(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(administrator.getId(), administrator.getName()));
    }

    @GetMapping
    @Operation(summary = "Listar administradores",
            description = "Operação exclusiva de administradores com papel ADMIN.",
            security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<List<AdministratorResponse>> findAll() {
        List<AdministratorResponse> administrators = findAllUserAdmUseCase.execute()
                .stream()
                .map(AdministratorResponse::from)
                .toList();
        return ResponseEntity.ok(administrators);
    }

    @GetMapping("/{administratorId}/financial-summary")
    @Operation(summary = "Consultar resumo financeiro",
            security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<AdministratorFinancialSummaryResponse> financialSummary(
            @PathVariable java.util.UUID administratorId,
            Authentication authentication) {
        return ResponseEntity.ok(AdministratorFinancialSummaryResponse.from(
                administratorId,
                financialSummaryUseCase.execute(administratorId, authentication.getName())
        ));
    }
}
