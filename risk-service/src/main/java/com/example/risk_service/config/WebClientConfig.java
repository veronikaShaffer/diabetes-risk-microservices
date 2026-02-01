package com.example.risk_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${gateway.user}") String user,
                               @Value("${gateway.pass}") String pass) {
        return builder.defaultHeaders(h -> h.setBasicAuth(user, pass)).build();
    }
}
