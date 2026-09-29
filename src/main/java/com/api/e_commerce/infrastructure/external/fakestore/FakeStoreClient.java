package com.api.e_commerce.infrastructure.external.fakestore;

import com.api.e_commerce.domain.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class FakeStoreClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FakeStoreClient.class);
    private final RestClient restClient;

    public FakeStoreClient(@Qualifier("fakeStoreRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<FakeStoreProductResponse> findAllProducts() {
        try {
            List<FakeStoreProductResponse> products = restClient.get()
                    .uri("/products")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return products == null ? List.of() : products;
        } catch (RestClientException exception) {
            LOGGER.warn("Fake Store API request failed: {}", exception.getClass().getSimpleName());
            throw new ExternalApiException("Fake Store API is temporarily unavailable", exception);
        }
    }
}
