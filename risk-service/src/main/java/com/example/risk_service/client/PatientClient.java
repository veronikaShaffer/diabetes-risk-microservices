package com.example.risk_service.client;

import com.example.risk_service.dto.PatientDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PatientClient {

    private final WebClient webClient;

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    public PatientClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public PatientDto getPatientById(String patientId) {
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/patients/{id}", patientId)
                .retrieve()
                .bodyToMono(PatientDto.class)
                .block();
    }
}
