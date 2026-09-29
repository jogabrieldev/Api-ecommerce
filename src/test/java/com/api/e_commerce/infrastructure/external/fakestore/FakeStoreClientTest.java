package com.api.e_commerce.infrastructure.external.fakestore;

import com.api.e_commerce.domain.exception.ExternalApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FakeStoreClientTest {
    @Test
    void shouldDeserializeProductsWithoutCallingRealApi() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://fake-store.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://fake-store.test/products"))
                .andRespond(withSuccess("""
                        [{"id":1,"title":"Product","price":10.50,"description":"Desc",
                          "category":"electronics","image":"https://img.test/1.png",
                          "rating":{"rate":4.0,"count":5}}]
                        """, MediaType.APPLICATION_JSON));

        var products = new FakeStoreClient(builder.build()).findAllProducts();

        assertEquals(1, products.size());
        assertEquals(1L, products.getFirst().id());
        server.verify();
    }

    @Test
    void shouldConvertExternalFailureToControlledException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://fake-store.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-store.test/products")).andRespond(withServerError());

        assertThrows(ExternalApiException.class,
                () -> new FakeStoreClient(builder.build()).findAllProducts());
        server.verify();
    }
}
