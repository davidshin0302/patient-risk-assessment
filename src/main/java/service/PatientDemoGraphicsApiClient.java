package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PatientDemoGraphicsApiClient {
    private final WebClient webClient;

    @Autowired
    public PatientDemoGraphicsApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Mono<String> fetchPatientDemoGraphicData() {
        return webClient.get()
                .uri("/patients")
                .retrieve()
                .bodyToMono(String.class);
    }
}
