package com.spheretech.flight_booking_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8000") // The Python FastAPI port
                .build();
    }
}