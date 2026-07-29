package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateCustomerUseCase;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.presentation.rest.request.CreateCustomerRequest;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
    }

    @PostMapping
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
}
