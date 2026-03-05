package com.guitteum.infra.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.embedding-base-url}")
    private String embeddingBaseUrl;

    @Bean
    public RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient geminiEmbeddingRestClient() {
        return RestClient.builder()
                .baseUrl(embeddingBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
