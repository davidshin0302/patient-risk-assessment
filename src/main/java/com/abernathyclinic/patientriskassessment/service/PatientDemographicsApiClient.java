package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service class to interact with the Patient Demographics API.
 * This class handles fetching patient demographic data from an external API.
 */
@Slf4j
@Service
public class PatientDemographicsApiClient {
    private final WebClient webClient;

    /**
     * Constructs a new PatientDemographicsApiClient.
     *
     * @param webClientBuilder The WebClient.Builder to configure the WebClient.
     * @param url The base URL of the Patient Demographics API.
     */
    @Autowired
    public PatientDemographicsApiClient(WebClient.Builder webClientBuilder, @Value("${patient-demographic.base-url}")  String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    /**
     * Fetches patient demographic data from the external API.
     *
     * @return A Mono of PatientListDTO containing the fetched data, or an empty Mono if an error occurs.
     */
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
