package com.example.risk_service.client;

import com.example.risk_service.dto.NoteDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Component
public class NotesClient {

    private final WebClient webClient;

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    public NotesClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<NoteDto> getNotesByPatientId(String patientId) {
        try {
            return webClient.get()
                    .uri(gatewayBaseUrl + "/api/patients/{id}/notes", patientId)
                    .retrieve()
                    .bodyToFlux(NoteDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
