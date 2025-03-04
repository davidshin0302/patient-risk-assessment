package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientRecordClient {
    private final WebClient webClient;

    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<PatientRecordDTO> fetchPatientRecords(String padId) {
        return webClient.get()
                .uri("/patHistory/get/?patId=" + padId)
                .retrieve()
                .bodyToMono(PatientRecordDTO.class)
                .doOnSuccess(patientRecordsDTO -> {
                    log.info("Successfully fetched patient records: {}", patientRecordsDTO);
                })
                .doOnError(error -> {
                    log.error("Error fetching patient records: {}", error.getMessage(), error);
                });
    }
}
