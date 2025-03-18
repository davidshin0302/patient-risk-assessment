package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientRecordClient {
    private final WebClient webClient;

    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder, @Value("${patient-record.base-url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    public Mono<PatientRecordDTO> fetchPatientRecords(String patId) {
        return webClient.get()
                .uri("/patHistory/get?patId=" + patId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("4xx Client Error Detected: {}", clientResponse.statusCode());
                    return Mono.empty();

                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("500 Client Error Detected: {}", response.statusCode());
                    return Mono.empty();
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
