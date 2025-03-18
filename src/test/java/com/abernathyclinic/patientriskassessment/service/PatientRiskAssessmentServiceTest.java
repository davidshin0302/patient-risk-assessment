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
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

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

    @Autowired
    ObjectMapper objectMapper;

    PatientRisk patientRisk;

    PatientRecordDTO patientRecordDTO;

    PatientRecordDTO invalidPatientRecord;

    PatientListDTO patientListDTO;

    String patId;

    @BeforeEach
    void setUp() throws IOException {
        patId = "1";
        String FILE_PATH = "src/test/resources/";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockPatientRiskData.json"), PatientRisk.class);
        patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecordData.json"), PatientRecordDTO.class);
        patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        invalidPatientRecord = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecordData_MissingPrefix.json"), PatientRecordDTO.class);

        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));
    }

    @Test
    void extractPatientName_missingPrefix() {
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(invalidPatientRecord));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);
        StepVerifier.create(patientRiskMono)
                .expectComplete();
    }

    @Test
    void getPatientRiskAssessment() {
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));

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

    @Test
    void getPatientRiskAssessment_returnEmptyMono() {
        PatientListDTO emptyPatientListDTO = new PatientListDTO();
        PatientRecordDTO emptyPatientRecordDTO = new PatientRecordDTO();
        emptyPatientRecordDTO.setClinicalNotes(new ArrayList<>());

        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(emptyPatientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(emptyPatientListDTO));


        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        StepVerifier.create(patientRiskMono)
                .expectComplete()
                .verify();
    }
}