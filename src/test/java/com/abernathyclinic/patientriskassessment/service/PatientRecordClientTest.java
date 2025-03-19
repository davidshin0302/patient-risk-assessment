package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PatientRecordClientTest {
    PatientRecordClient patientRecordClient;

    ObjectMapper objectMapper;

    MockWebServer mockWebServer;

    PatientRecordDTO patientRecordDTO;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String FILE_PATH = "src/test/resources/mockPatientRecordData.json";
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientRecordDTO = objectMapper.readValue(new File(FILE_PATH), PatientRecordDTO.class);

        String patId = "1";
        String url = mockWebServer.url("/patHistory/get?patId=" + patId).toString();
        patientRecordClient = new PatientRecordClient(WebClient.builder(), url);
    }

    @AfterEach
    void tearDwon() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchPatientRecords() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(patientRecordDTO)));

        String patId = "1";
        Mono<PatientRecordDTO> output = patientRecordClient.fetchPatientRecords(patId);
        StepVerifier.create(output)
                .expectNextMatches(patientRecrod -> {
                    assertNotNull(patientRecrod);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void fetchPatientRecords_NotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        String patId= "99";
        Mono<PatientRecordDTO> output = patientRecordClient.fetchPatientRecords(patId);
        StepVerifier.create(output)
                .verifyComplete();
    }

    @Test
    void fetchPatientRecords_InternalError(){
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        String patId= "2";
        Mono<PatientRecordDTO> output = patientRecordClient.fetchPatientRecords(patId);
        StepVerifier.create(output)
                .verifyComplete();
    }
}