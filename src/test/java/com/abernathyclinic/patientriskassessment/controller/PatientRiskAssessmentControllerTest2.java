package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PatientRiskAssessmentControllerTest2 {

    @Mock
    private PatientRiskAssessmentService patientRiskAssessmentService;

    @InjectMocks
    private PatientRiskAssessmentController patientRiskAssessmentController;

    @Test
    void getPatientRiskAssessmentById_shouldReturnOkStatus() {
        // Arrange
        String patId = "123";
        PatientRisk patientRisk = PatientRisk.builder().lastName("Doe").firstName("John").age("30").build();

        when(patientRiskAssessmentService.getPatientRiskAssessment(patId)).thenReturn(Mono.just(patientRisk));

        // Act
        Mono<ResponseEntity<PatientRisk>> responseMono = patientRiskAssessmentController.getPatientRiskAssessmentById(patId);

        // Assert
        StepVerifier.create(responseMono)
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }
}