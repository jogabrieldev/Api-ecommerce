package com.api.e_commerce.infrastructure.external.fakestore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class FakeStoreClientConfiguration {

    @Bean("fakeStoreRestClient")
    RestClient fakeStoreRestClient(
            RestClient.Builder builder,
            @Value("${external.api.fake-store.base-url}") String baseUrl,
            @Value("${external.api.fake-store.connect-timeout:3s}") Duration connectTimeout,
            @Value("${external.api.fake-store.read-timeout:10s}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
