package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
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
    PatientRecordClient testPatientRecordClientById;

    ObjectMapper objectMapper;

    MockWebServer mockWebServer;

    PatientRecordDTO patientRecordDTO;

    PatientRecordsDTO patientRecordsDTO;

    String FILE_PATH = "src/test/resources/";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();


        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestNone.json"), PatientRecordDTO.class);
        patientRecordsDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecords.json"), PatientRecordsDTO.class);

        String patId = "1";
        String url = mockWebServer.url("/patHistory/get?patId=" + patId).toString();
        testPatientRecordClientById = new PatientRecordClient(WebClient.builder(), url);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchPatientRecordsById() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(patientRecordDTO)));

        String patId = "1";
        Mono<PatientRecordDTO> output = testPatientRecordClientById.fetchPatientRecordsById(patId);
        StepVerifier.create(output)
                .expectNextMatches(patientRecord -> {
                    assertNotNull(patientRecord);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void fetchPatientRecordsById_NotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        String patId = "99";
        Mono<PatientRecordDTO> output = testPatientRecordClientById.fetchPatientRecordsById(patId);
        StepVerifier.create(output)
                .verifyComplete();
    }

    @Test
    void fetchPatientRecordsById_InternalError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        String patId = "2";
        Mono<PatientRecordDTO> output = testPatientRecordClientById.fetchPatientRecordsById(patId);
        StepVerifier.create(output)
                .verifyComplete();
    }

    @Test
    void fetchPatientRecordsByFamilyLastName() throws IOException {
        String url = mockWebServer.url("/patHistory/get/patient-records").toString();
        PatientRecordClient testPatientRecordClientByFamilyName = new PatientRecordClient(WebClient.builder(), url);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(patientRecordsDTO)));

        Mono<PatientRecordsDTO> output = testPatientRecordClientById.fetchAllPatientRecords();
        StepVerifier.create(output)
                .expectNextMatches(patientRecords -> {
                    assertNotNull(patientRecords);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void fetchPatientRecordsByFamilyLastName_NotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Mono<PatientRecordsDTO> output = testPatientRecordClientById.fetchAllPatientRecords();
        StepVerifier.create(output)
                .verifyComplete();
    }

    @Test
    void fetchPatientRecordsByFamilyLastName_InternalError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        Mono<PatientRecordsDTO> output = testPatientRecordClientById.fetchAllPatientRecords();
        StepVerifier.create(output)
                .verifyComplete();
    }
}