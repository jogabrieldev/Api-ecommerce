package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateCustomerUseCase;
import com.api.e_commerce.application.usecase.FindAuthenticatedCustomerUseCase;
import com.api.e_commerce.application.usecase.FindAllCustomersUseCase;
import com.api.e_commerce.application.usecase.FindCustomerByEmailUseCase;
import com.api.e_commerce.application.usecase.AnalyzeCustomerPurchasesUseCase;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.presentation.rest.request.CreateCustomerRequest;
import com.api.e_commerce.presentation.rest.request.CustomerAuthenticationRequest;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import com.api.e_commerce.presentation.rest.response.CustomerTokenResponse;
import com.api.e_commerce.presentation.rest.response.CustomerResponse;
import com.api.e_commerce.presentation.rest.response.CustomerPurchaseAnalysisResponse;
import com.api.e_commerce.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customers")
@Tag(name = "Clientes", description = "Cadastro, autenticação e consultas")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final FindAuthenticatedCustomerUseCase findAuthenticatedCustomerUseCase;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final FindAllCustomersUseCase findAllCustomersUseCase;
    private final FindCustomerByEmailUseCase findCustomerByEmailUseCase;
    private final AnalyzeCustomerPurchasesUseCase analyzeCustomerPurchasesUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase,
                              FindAuthenticatedCustomerUseCase findAuthenticatedCustomerUseCase,
                              AuthenticationManager authenticationManager,
                              JwtService jwtService,
                              FindAllCustomersUseCase findAllCustomersUseCase,
                              FindCustomerByEmailUseCase findCustomerByEmailUseCase,
                              AnalyzeCustomerPurchasesUseCase analyzeCustomerPurchasesUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.findAuthenticatedCustomerUseCase = findAuthenticatedCustomerUseCase;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.findAllCustomersUseCase = findAllCustomersUseCase;
        this.findCustomerByEmailUseCase = findCustomerByEmailUseCase;
        this.analyzeCustomerPurchasesUseCase = analyzeCustomerPurchasesUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        CreateCustomerRequest.AddressRequest address = request.address();
        Customer customer = createCustomerUseCase.execute(
                request.name(),
                request.email(),
                request.password(),
                request.cpf(),
                request.phone(),
                request.birthDate(),
                new CreateCustomerUseCase.AddressData(
                        address.zipCode(),
                        address.street(),
                        address.number(),
                        address.complement(),
                        address.neighborhood(),
                        address.city(),
                        address.state()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(customer.getId(), customer.getName()));
    }

    @PostMapping("/authentication")
    @Operation(summary = "Autenticar cliente e emitir JWT")
    public ResponseEntity<CustomerTokenResponse> authenticate(
            @Valid @RequestBody CustomerAuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email().trim().toLowerCase(),
                        request.password()
                )
        );
        boolean customer = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("CUSTOMER_CHECKOUT"));
        if (!customer) {
            throw new BadCredentialsException("Invalid customer credentials");
        }

        Customer authenticatedCustomer = findAuthenticatedCustomerUseCase.execute(authentication.getName());
        JwtService.Token token = jwtService.generate(authenticatedCustomer.getEmail());
        return ResponseEntity.ok(CustomerTokenResponse.from(authenticatedCustomer, token.value(), token.expiresIn()));
    }

    @GetMapping
    @Operation(summary = "Listar clientes")
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(findAllCustomersUseCase.execute()
                .stream()
                .map(CustomerResponse::from)
                .toList());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Consultar cliente por e-mail")
    public ResponseEntity<CustomerResponse> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(CustomerResponse.from(findCustomerByEmailUseCase.execute(email)));
    }

    @GetMapping("/email/{email}/purchase-analysis")
    @Operation(summary = "Analisar compras do cliente")
    public ResponseEntity<CustomerPurchaseAnalysisResponse> analyzePurchases(@PathVariable String email) {
        return ResponseEntity.ok(CustomerPurchaseAnalysisResponse.from(analyzeCustomerPurchasesUseCase.execute(email)));
    }
}
