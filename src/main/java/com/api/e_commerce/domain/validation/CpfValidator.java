package com.api.e_commerce.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String cpf = value.replaceAll("\\D", "");
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(cpf, 9, 10);
        int secondDigit = calculateDigit(cpf, 10, 11);
        return firstDigit == Character.getNumericValue(cpf.charAt(9))
                && secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateDigit(String cpf, int length, int initialWeight) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(cpf.charAt(index)) * (initialWeight - index);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
