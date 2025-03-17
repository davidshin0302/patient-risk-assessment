package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.service.PatientDemographicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

@WebFluxTest(PatientRiskAssessmentController.class)
class PatientRiskAssessmentControllerTest {
    static MockWebServer mockBackEnd;

    @MockitoBean
    PatientRiskAssessmentService patientRiskAssessmentService;

    @MockitoBean
    PatientRecordClient patientRecordClient;

    @MockitoBean
    PatientDemographicsApiClient patientDemographicsApiClient;

    @Autowired
    WebTestClient webTestClient;

    PatientListDTO patientListDTOMono;

    PatientRecordDTO patientRecordDTOMono;

    PatientRisk patientRiskMono;

    @BeforeAll
    static void setUpMockWebServer() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDownMockWebServer() throws IOException {
        mockBackEnd.shutdown();
    }


    @BeforeEach
    void setUp() throws IOException {
        String FILE_PATH = "src/test/java/com/abernathyclinic/patientriskassessment/resources/";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientListDTOMono = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        patientRecordDTOMono = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecordData.json"), PatientRecordDTO.class);
        patientRiskMono = objectMapper.readValue(new File(FILE_PATH + "mockPatientRiskData.json"), PatientRisk.class);

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTOMono));
        when(patientRecordClient.fetchPatientRecords(any(String.class))).thenReturn(Mono.just(patientRecordDTOMono));
        when(patientRiskAssessmentService.getPatientRiskAssessment(any(String.class))).thenReturn(Mono.just(patientRiskMono));

    }

    @Test
    void getPatientRiskAssessmentById() throws Exception {
        webTestClient.get().uri("/assess/1")
                .exchange()
                .expectStatus().isOk();
    }
}