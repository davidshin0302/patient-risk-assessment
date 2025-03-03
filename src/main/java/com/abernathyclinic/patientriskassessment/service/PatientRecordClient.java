package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PatientRecordClient {
    private final WebClient webClient;

    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:8082").build();
    }

    public Mono<PatientRecordsDTO> fetchPatientRecords() {
        return webClient.get() // HTTP GET request
                .uri("/patHistory/get/patient-records") //Path of the endPoint
                .retrieve() // Initiate the request and retrieve the response
                .bodyToMono(PatientRecordsDTO.class)
                .log("PatentRecrods!!!!!!!!! ")
                .doOnNext(dto -> {
                    System.out.println("Debugggin DTO: " + dto);
                }); // Convert response body to a Mono<String>
    }
}
