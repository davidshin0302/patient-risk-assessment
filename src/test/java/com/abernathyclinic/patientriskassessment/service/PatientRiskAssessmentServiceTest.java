package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PatientRiskAssessmentServiceTest {
    @InjectMocks
    PatientRiskAssessmentService patientRiskAssessmentService;

    @Mock
    PatientRecordClient patientRecordClient;

    @Mock
    PatientDemographicsApiClient patientDemographicsApiClient;

    ObjectMapper objectMapper;

    String FILE_PATH = "src/test/resources/";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    //Helper Method
    boolean assertPatientRiskAssessment(PatientListDTO patientListDTO, PatientRecordDTO patientRecordDTO, PatientRisk expectedRisk, String patId) {
        boolean result = false;
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        try {
            StepVerifier.create(patientRiskMono)
                    .expectNextMatches(actualRisk -> {
                        assertEquals(expectedRisk.getAge(), actualRisk.getAge());
                        assertEquals(expectedRisk.getLastName(), actualRisk.getLastName());
                        assertEquals(expectedRisk.getFirstName(), actualRisk.getFirstName());
                        assertEquals(expectedRisk.getRiskLevel(), actualRisk.getRiskLevel());
                        return true;
                    })
                    .verifyComplete();
            result = true;
        } catch (AssertionError e) {
            log.error(e.getMessage());
        }
        return result;
    }


    @Test
    void assertPatientRiskAssessment_testNone() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestNone.json"), PatientRecordDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelNoneData.json"), PatientRisk.class);
        String patId = "11";

        assertTrue(assertPatientRiskAssessment(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_test_borderline() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestBorderLine.json"), PatientRecordDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelBorderLineData.json"), PatientRisk.class);
        String patId = "12";

        assertTrue(assertPatientRiskAssessment(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_test_in_danger() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelEarlyOnset.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestEarlyOnset.json"), PatientRecordDTO.class);
        String patId = "13";


        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        assertTrue(assertPatientRiskAssessment(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_test_early_onset() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelInDanger.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger.json"), PatientRecordDTO.class);
        String patId = "14";


        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        assertTrue(assertPatientRiskAssessment(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_patientNotFound() {
        String patId = "123";
        PatientListDTO patientListDTONotFound = new PatientListDTO();
        PatientDTO patient = new PatientDTO();
        patient.setFamilyName("david");

        List<PatientDTO> patientDTOList = new ArrayList<>();
        patientListDTONotFound.setPatientList(patientDTOList);

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTONotFound));

        PatientRecordDTO patientRecordDTO = new PatientRecordDTO();
        ClinicalNoteDTO clinicalNoteDTO = new ClinicalNoteDTO();
        clinicalNoteDTO.setNote("patient: shin");
        patientRecordDTO.setClinicalNotes(Collections.singletonList(clinicalNoteDTO));

        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(patId))
                .expectComplete()
                .verify();
    }

    @Test
    void assertPatientRiskAssessment_returnEmptyMono() throws IOException {
        String invalidId = "#";
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger.json"), PatientRecordDTO.class);

        patientRecordDTO.setClinicalNotes(null);
        when(patientRecordClient.fetchPatientRecords(invalidId)).thenReturn(Mono.just(patientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(new PatientListDTO()));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(invalidId))
                .expectComplete()
                .verify();

        patientRecordDTO.setClinicalNotes(Collections.emptyList());
        when(patientRecordClient.fetchPatientRecords(invalidId)).thenReturn(Mono.just(patientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(new PatientListDTO()));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(invalidId))
                .expectComplete()
                .verify();
    }

    @Test
    void getPatientRiskAssessment_shouldReturnEmpty_whenNoMatchingPatient() {
        String patId = "789";

        PatientDTO differentPatient = new PatientDTO();
        differentPatient.setFamilyName("Doe"); // Mismatch
        differentPatient.setGivenName("Jane");
        differentPatient.setDateOfBirth("1990-02-15");
        differentPatient.setSex("F");

        ClinicalNoteDTO clinicalNote = new ClinicalNoteDTO();
        clinicalNote.setNote("Patient: Smith. Diagnosed with hypertension."); // Smith, but no matching patient

        PatientListDTO patientList = new PatientListDTO();
        patientList.setPatientList(Collections.singletonList(differentPatient));

        PatientRecordDTO patientRecordDTO = new PatientRecordDTO();
        patientRecordDTO.setClinicalNotes(Collections.singletonList(clinicalNote));

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientList));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(patId))
                .expectComplete() // No match, so should return Mono.empty()
                .verify();
    }
}