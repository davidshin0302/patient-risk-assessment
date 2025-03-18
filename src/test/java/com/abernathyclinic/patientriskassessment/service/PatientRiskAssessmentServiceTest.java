package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientRiskAssessmentServiceTest {
    @InjectMocks
    PatientRiskAssessmentService patientRiskAssessmentService;

    @Mock
    PatientRecordClient patientRecordClient;

    @Mock
    PatientDemographicsApiClient patientDemographicsApiClient;

    PatientRisk patientRisk;

    PatientRecordDTO patientRecordDTO;

    PatientListDTO patientListDTO;

    @BeforeEach
    void setUp() throws IOException {
        String patId = "1";
        String FILE_PATH = "src/test/resources/";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockPatientRiskData.json"), PatientRisk.class);
        patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecordData.json"), PatientRecordDTO.class);
        patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);

        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
    }

//    @Test
//    void extractPatientName() {
//    }
//
//    @Test
//    void ageCalculator() {
//    }
//
//    @Test
//    void buildPatientRisk() {
//    }

    @Test
    void getPatientRiskAssessment() {
        String patId = "1";
        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);
        StepVerifier.create(patientRiskMono)
                .expectNextMatches(dto -> {
                    assertEquals(dto.getAge(), patientRisk.getAge());
                    assertEquals(dto.getLastName(), patientRisk.getLastName());
                    assertEquals(dto.getFirstName(), patientRisk.getFirstName());
                    return true;
                })
                .verifyComplete();
    }
}