package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientRecordClient {
    private final WebClient webClient;

    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<PatientRecordDTO> fetchPatientRecords(String patId) {
        return webClient.get()
                .uri("/patHistory/get?patId=" + patId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new WebClientResponseException(
                                    clientResponse.statusCode().value(),
                                    "Client Error: " + clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(),
                                    errorBody.getBytes(),
                                    null
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new WebClientResponseException(
                                    response.statusCode().value(),
                                    "Server Error: " + response.statusCode(),
                                    response.headers().asHttpHeaders(),
                                    errorBody.getBytes(),
                                    null
                            )));
                })
                .bodyToMono(PatientRecordDTO.class)
                .doOnSuccess(patientRecordDTO -> {
                    log.info("Successfully fetched patient records: {}", patientRecordDTO);
                })
                .doOnError(error -> {
                    log.error("Error fetching patient records: {}", error.getMessage(), error);
                });
    }
}
