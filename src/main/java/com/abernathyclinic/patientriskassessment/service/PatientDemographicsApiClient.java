package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientDemographicsApiClient {
    private final WebClient webClient;

    @Autowired
    public PatientDemographicsApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Mono<PatientListDTO> fetchPatientDemoGraphicData() {
        return webClient.get()
                .uri("/patients")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("4xx Client Error Detected: {}", clientResponse.statusCode());
                    return Mono.empty();

                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("500 Client Error Detected: {}", response.statusCode());
                    return Mono.empty();
                })
                .bodyToMono(PatientListDTO.class)
                .doOnSuccess(patientListDTO -> {
                    log.info("Successfully fetched list of patient demographic : {}", patientListDTO);
                })
                .doOnError(error -> {
                    log.error("Error fetching list of patient demographic: {}", error.getMessage(), error);
                });
    }
}
