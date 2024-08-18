package com.sinignaci.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final ClientProperties clientProperties;

    @Bean
    WebClient webClient(WebClient.Builder webClientBuilder) {
        return WebClient.builder()
                .baseUrl(clientProperties.catalogServiceUri().toString())
                .build();
    }
}
