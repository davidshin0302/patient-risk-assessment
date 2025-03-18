package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
class PatientDemographicsApiClientTest {
    PatientDemographicsApiClient patientDemographicsApiClient;

    ObjectMapper objectMapper;

    MockWebServer mockWebServer;

    PatientListDTO patientListDTO;


    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String FILE_PATH = "src/test/resources/mockPatientData.json";
        objectMapper = new ObjectMapper();

        patientListDTO = objectMapper.readValue(new File(FILE_PATH), PatientListDTO.class);

        String url = mockWebServer.url("/patients").toString();
        patientDemographicsApiClient = new PatientDemographicsApiClient(WebClient.builder(), url);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchPatientDemoGraphicData() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(patientListDTO)));

        Mono<PatientListDTO> output = patientDemographicsApiClient.fetchPatientDemoGraphicData();

        StepVerifier.create(output)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    void fetchPatientDemoGraphicData_NotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        StepVerifier.create(patientDemographicsApiClient.fetchPatientDemoGraphicData())
                .verifyComplete();
    }

    @Test
    void fetchPatientDemoGrpahicData_InternalError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        StepVerifier.create(patientDemographicsApiClient.fetchPatientDemoGraphicData())
                .verifyComplete();
    }
}