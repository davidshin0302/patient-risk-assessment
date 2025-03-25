package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service class to interact with the Patient Record API.
 * This class handles fetching patient record data from an external API.
 */
@Slf4j
@Service
public class PatientRecordClient {
    private final WebClient webClient;

    /**
     * Constructs a new PatientRecordClient.
     *
     * @param webClientBuilder The WebClient.Builder to configure the WebClient.
     * @param url              The base URL of the Patient Record API.
     */
    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder, @Value("${patient-record.base-url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    /**
     * Fetches patient record data from the external API based on patient ID.
     *
     * @param patId The patient ID to search for.
     * @return A Mono of PatientRecordDTO containing the fetched data, or an empty Mono if an error occurs.
     */
    public Mono<PatientRecordDTO> fetchPatientRecordsById(String patId) {
        return webClient.get().uri("/patHistory/get?patId=" + patId).retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            log.error("4xx Client Error Detected: {}", clientResponse.statusCode());
            return Mono.empty();

        }).onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("500 Client Error Detected: {}", response.statusCode());
            return Mono.empty();
        }).bodyToMono(PatientRecordDTO.class).doOnSuccess(patientRecordDTO -> {
            log.info("Successfully fetched patient records: {}", patientRecordDTO);
        }).doOnError(error -> {
            log.error("Error fetching patient records: {}", error.getMessage(), error);
        });
    }

    /**
     * Fetches all patient records from an external service.
     *
     * @return A Mono of PatientRecordsDTO containing the fetched records, or an empty Mono if an error occurs.
     * Handles 4xx and 5xx errors by logging and returning an empty Mono.
     */
    public Mono<PatientRecordsDTO> fetchAllPatientRecords() {
        return webClient.get().uri("/patHistory/get/patient-records").retrieve().onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            log.error("4xx Client Error Detected: {}", clientResponse.statusCode());
            return Mono.empty();

        }).onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("500 Client Error Detected: {}", response.statusCode());
            return Mono.empty();
        }).bodyToMono(PatientRecordsDTO.class).doOnSuccess(patientRecordsDTO -> {
            log.info("Successfully fetched patient records: {}", patientRecordsDTO);
        }).doOnError(error -> {
            log.error("Error fetching patient records: {}", error.getMessage(), error);
        });
    }
}
