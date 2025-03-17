package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PatientRiskAssessmentControllerTest {
    @Mock
    PatientRiskAssessmentService patientRiskAssessmentService;

    @InjectMocks
    PatientRiskAssessmentController patientRiskAssessmentController;

    PatientRisk patientRisk;

    @BeforeEach
    void setUp() throws IOException {
        String FILE_PATH = "src/test/java/com/abernathyclinic/patientriskassessment/resources/";
        ObjectMapper objectMapper = new ObjectMapper();

        patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockPatientRiskData.json"), PatientRisk.class);

        when(patientRiskAssessmentService.getPatientRiskAssessment(anyString())).thenReturn(Mono.just(patientRisk));
    }

    @Test
    void getPatientRiskAssessmentById_shouldReturnOkStatus() {
        // Act
        Mono<ResponseEntity<PatientRisk>> responseMono = patientRiskAssessmentController.getPatientRiskAssessmentById(anyString());

        // Assert
        StepVerifier.create(responseMono)
                .expectNextMatches(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertEquals(patientRisk.getFirstName(), responseEntity.getBody().getFirstName());
                    assertEquals(patientRisk.getLastName(), responseEntity.getBody().getLastName());
                    assertEquals(patientRisk.getAge(), responseEntity.getBody().getAge());
                    return true;
                })
                .verifyComplete();
    }
}