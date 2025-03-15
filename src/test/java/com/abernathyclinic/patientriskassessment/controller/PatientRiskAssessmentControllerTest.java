package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.service.PatientDemographicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

@WebMvcTest(PatientRiskAssessmentController.class)
class PatientRiskAssessmentControllerTest {
    @InjectMocks
    PatientRiskAssessmentController patientRiskAssessmentController;

    @MockitoBean
    PatientRiskAssessmentService patientRiskAssessmentService;

    @MockitoBean
    PatientRecordClient patientRecordClient;

    @MockitoBean
    PatientDemographicsApiClient patientDemographicsApiClient;

    @Autowired
    WebTestClient webTestClient;

    Mono<PatientListDTO> patientListDTOMono;

    Mono<PatientRecordDTO> patientRecordDTOMono;

    Mono<PatientRisk> patientRiskMono;

    @BeforeEach
    void setUp() throws IOException {
        String FILE_PATH = "src/test/java/com/abernathyclinic/patientriskassessment/resources/";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientListDTOMono = Mono.just(objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class));
        patientRecordDTOMono = Mono.just(objectMapper.readValue(new File(FILE_PATH + "mockPatientRecordData.json"), PatientRecordDTO.class));
        patientRiskMono = Mono.just(objectMapper.readValue(new File(FILE_PATH + "mockPatientRiskData.json"), PatientRisk.class));


        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(patientListDTOMono);
        when(patientRecordClient.fetchPatientRecords(any(String.class))).thenReturn(patientRecordDTOMono);
        when(patientRiskAssessmentService.getPatientRiskAssessment(any(String.class))).thenReturn(patientRiskMono);

        webTestClient = WebTestClient.bindToController(patientRiskAssessmentController).build();
    }

    @Test
    void getPatientRiskAssessmentById() throws Exception {
        webTestClient.get().uri("/assess/1")
                .exchange()
                .expectStatus().isOk();
    }
}