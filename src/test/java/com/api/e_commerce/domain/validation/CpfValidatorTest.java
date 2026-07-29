package com.api.e_commerce.domain.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidCpf() {
        assertTrue(validator.validate(new CpfPayload("52998224725")).isEmpty());
        assertTrue(validator.validate(new CpfPayload("529.982.247-25")).isEmpty());
    }

    @Test
    void shouldRejectInvalidAndRepeatedCpf() {
        assertFalse(validator.validate(new CpfPayload("52998224724")).isEmpty());
        assertFalse(validator.validate(new CpfPayload("11111111111")).isEmpty());
    }

    private record CpfPayload(@NotBlank @ValidCpf String cpf) {
    }
}
