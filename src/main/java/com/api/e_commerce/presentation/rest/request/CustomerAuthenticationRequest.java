package com.api.e_commerce.presentation.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerAuthenticationRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
