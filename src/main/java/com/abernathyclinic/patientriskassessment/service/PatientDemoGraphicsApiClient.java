package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientDemoGraphicsApiClient {
    private final WebClient webClient;

    @Autowired
    public PatientDemoGraphicsApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Mono<PatientListDTO> fetchPatientDemoGraphicData() {
        return webClient.get()
                .uri("/patients")
                .retrieve()
                .bodyToMono(PatientListDTO.class);
    }
}
