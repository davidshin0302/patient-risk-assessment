package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
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

import static org.junit.jupiter.api.Assertions.*;
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

    //Helper Method for risk assessment by Id
    boolean assertPatientRiskAssessment_byId(PatientListDTO patientListDTO, PatientRecordDTO patientRecordDTO, PatientRisk expectedRisk, String patId) {
        boolean result = false;
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessmentById(patId);

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

    //Helper Method for risk assessment by  familyName
    boolean assertPatientRiskAssessment_by_familyName(PatientListDTO patientListDTO, PatientRecordsDTO patientRecordsDTO, PatientRisk expectedRisk, String familyName) {
        boolean result = false;
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchAllPatientRecords()).thenReturn(Mono.just(patientRecordsDTO));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessmentByFamilyName(familyName);

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
    void assertPatientRiskAssessment_by_id_testNone() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestNone.json"), PatientRecordDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelNoneData.json"), PatientRisk.class);
        String patId = "11";

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_by_id_test_borderline() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestBorderLine.json"), PatientRecordDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelBorderLine.json"), PatientRisk.class);
        String patId = "12";

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO, patientRisk, patId));
    }

    @Test
    void assertPatientRiskAssessment_by_id_test_early_onset() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelEarlyOnset.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestEarlyOnset.json"), PatientRecordDTO.class);
        String patId = "13";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO, patientRisk, patId));

        PatientRisk patientRisk2 = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelEarlyOnset2.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO2 = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestEarlyOnset2.json"), PatientRecordDTO.class);
        patId = "16";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO2));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO2, patientRisk2, patId));

        PatientRisk patientRisk3 = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelEarlyOnset3.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO3 = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestEarlyOnset3.json"), PatientRecordDTO.class);
        patId = "17";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO3));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO3, patientRisk3, patId));
    }

    @Test
    void assertPatientRiskAssessment_by_id_test_in_danger() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelInDanger.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger.json"), PatientRecordDTO.class);
        String patId = "14";


        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO, patientRisk, patId));

        PatientRisk patientRisk2 = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelInDanger2.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO2 = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger2.json"), PatientRecordDTO.class);
        patId = "15";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO2));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO2, patientRisk2, patId));

        PatientRisk patientRisk3 = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelInDanger3.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO3 = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger3.json"), PatientRecordDTO.class);
        patId = "18";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO3));

        assertTrue(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO3, patientRisk3, patId));

        PatientRisk patientRisk4 = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelInDanger4.json"), PatientRisk.class);
        PatientRecordDTO patientRecordDTO4 = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger4.json"), PatientRecordDTO.class);
        patId = "18";

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO4));

        assertFalse(assertPatientRiskAssessment_byId(patientListDTO, patientRecordDTO4, patientRisk4, patId));
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

        when(patientRecordClient.fetchPatientRecordsById(patId)).thenReturn(Mono.just(patientRecordDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessmentById(patId))
                .expectComplete()
                .verify();
    }

    @Test
    void assertPatientRiskAssessment_returnEmptyMono() throws IOException {
        String invalidId = "#";
        PatientRecordDTO patientRecordDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientNoteTestInDanger.json"), PatientRecordDTO.class);

        patientRecordDTO.setClinicalNotes(null);
        when(patientRecordClient.fetchPatientRecordsById(invalidId)).thenReturn(Mono.just(patientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(new PatientListDTO()));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessmentById(invalidId))
                .expectComplete()
                .verify();

        patientRecordDTO.setClinicalNotes(Collections.emptyList());
        when(patientRecordClient.fetchPatientRecordsById(invalidId)).thenReturn(Mono.just(patientRecordDTO));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(new PatientListDTO()));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessmentById(invalidId))
                .expectComplete()
                .verify();
    }

    @Test
    void assertPatientRiskAssessment_testNone() throws IOException {
        PatientListDTO patientListDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientData.json"), PatientListDTO.class);
        PatientRecordsDTO patientRecordsDTO = objectMapper.readValue(new File(FILE_PATH + "mockPatientRecords.json"), PatientRecordsDTO.class);
        PatientRisk patientRisk = objectMapper.readValue(new File(FILE_PATH + "mockRiskLevelNoneData.json"), PatientRisk.class);
        String familyName = "testNone";

        assertTrue(assertPatientRiskAssessment_by_familyName(patientListDTO, patientRecordsDTO, patientRisk, familyName));
    }
}