package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.CustomerAddress;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerResponseTest {

    @Test
    void shouldMinimizeAndMaskPersonalData() {
        Customer customer = new Customer(
                "Customer",
                "customer@email.com",
                "password-hash",
                "52998224725",
                "11999998888",
                LocalDate.of(1990, 1, 1),
                new CustomerAddress(
                        "01310100", "Street", "10", null,
                        "District", "City", "SP")
        );

        CustomerResponse response = CustomerResponse.from(customer);

        assertEquals("cu***@email.com", response.maskedEmail());
        assertEquals("***.***.***-25", response.maskedCpf());
    }
}
