package com.api.e_commerce.presentation.rest.request;

import com.api.e_commerce.domain.validation.ValidCpf;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank
        @Pattern(regexp = "(?:\\d[.\\-]?){11}", message = "CPF must contain 11 digits")
        @ValidCpf String cpf,
        @NotBlank
        @Pattern(regexp = "\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4}", message = "Phone must contain area code and 10 or 11 digits")
        String phone,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotNull @Valid AddressRequest address
) {

    public record AddressRequest(
            @NotBlank
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "ZIP code must contain 8 digits") String zipCode,
            @NotBlank @Size(max = 150) String street,
            @NotBlank @Size(max = 20) String number,
            @Size(max = 100) String complement,
            @NotBlank @Size(max = 100) String neighborhood,
            @NotBlank @Size(max = 100) String city,
            @NotBlank
            @Pattern(regexp = "[A-Za-z]{2}", message = "State must contain its 2-letter code")
            String state
    ) {
    }
}
