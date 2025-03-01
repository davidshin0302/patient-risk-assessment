package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PatientRecordClient {
    private final WebClient webClient;

    @Autowired
    public PatientRecordClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<String> fetchPatientRecords() {
        return webClient.get() // HTTP GET request
                .uri("/patHistory/get/patient-records") //Path of the endPoint
                .retrieve() // Initiate the request and retrieve the response
                .bodyToMono(String.class); // Convert response body to a Mono<String>
    }
}
