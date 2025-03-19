package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientDTO;
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
import java.util.*;

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
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

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
    void patientRecordClient_emptyMono() {
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.empty());
        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        StepVerifier.create(patientRiskMono).expectComplete();
    }

    @Test
    void getPatientRiskAssessment_patientRecordApiReturnsEmpty() {
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.empty());

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        StepVerifier.create(patientRiskMono)
                .expectComplete()
                .verify();
    }

    @Test
    void getPatientRiskAssessment_emptyNote() {
        PatientRecordDTO patientRecordDTOEmptyNote = new PatientRecordDTO();
        ClinicalNoteDTO clinicalNoteDTO = new ClinicalNoteDTO();
        clinicalNoteDTO.setNote("");

        List<ClinicalNoteDTO> clinicalNotes = new ArrayList<>();
        clinicalNotes.add(clinicalNoteDTO);

        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTOEmptyNote));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(patId))
                .expectComplete()
                .verify();
    }

    @Test
    void getPatientRiskAssessment_patientNotFound() {
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
    void getPatientRiskAssessment_nullClinicalNotes() {
        PatientRecordDTO patientRecordDTONullNotes = new PatientRecordDTO();
        patientRecordDTONullNotes.setClinicalNotes(null);
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTONullNotes));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(patId))
                .expectComplete()
                .verify();
    }

    @Test
    void getPatientRiskAssessment_emptyClinicalNotes() {
        PatientRecordDTO patientRecordDTOEmptyNotes = new PatientRecordDTO();
        patientRecordDTOEmptyNotes.setClinicalNotes(new LinkedList<>());
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTOEmptyNotes));
        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));

        StepVerifier.create(patientRiskAssessmentService.getPatientRiskAssessment(patId))
                .expectComplete()
                .verify();
    }

    @Test
    void testGetPatientRiskAssessment_patientNotFound() {
        String lastName = "NonExistentLastName";

        // Mock PatientDemographicsApiClient to return a list with no matching last name
        PatientDTO patientDTO1 = new PatientDTO();
        patientDTO1.setFamilyName("shin");

        PatientDTO patientDTO2 = new PatientDTO();
        patientDTO2.setFamilyName("kim");

        PatientListDTO patientListDTO = new PatientListDTO();
        patientListDTO.setPatientList(Arrays.asList(patientDTO1, patientDTO2));

        // Mock PatientRecordClient to return valid patient data but no matching clinical note
        PatientRecordDTO patientRecordDTO = new PatientRecordDTO();
        ClinicalNoteDTO clinicalNoteDTO = new ClinicalNoteDTO();
        clinicalNoteDTO.setNote("random clinical note");

        patientRecordDTO.setClinicalNotes(Collections.singletonList(clinicalNoteDTO));

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        StepVerifier.create(patientRiskMono)
                .expectComplete() // Expect the result to complete without any patient risk found
                .verify();
    }

    @Test
    void testGetPatientRiskAssessment_patientDTOWithNullFields() {
        String lastName = "lee";

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFamilyName(lastName);
        patientDTO.setGivenName(null);
        patientDTO.setDateOfBirth(null);

        PatientListDTO patientListDTO = new PatientListDTO();
        patientListDTO.setPatientList(Collections.singletonList(patientDTO));

        PatientRecordDTO patientRecordDTO = new PatientRecordDTO();
        ClinicalNoteDTO clinicalNoteDTO = new ClinicalNoteDTO();
        clinicalNoteDTO.setNote("Patient: shin");
        patientRecordDTO.setClinicalNotes(Collections.singletonList(clinicalNoteDTO));

        when(patientDemographicsApiClient.fetchPatientDemoGraphicData()).thenReturn(Mono.just(patientListDTO));
        when(patientRecordClient.fetchPatientRecords(patId)).thenReturn(Mono.just(patientRecordDTO));

        Mono<PatientRisk> patientRiskMono = patientRiskAssessmentService.getPatientRiskAssessment(patId);

        StepVerifier.create(patientRiskMono)
                .expectComplete() // Expecting the Mono to complete without emitting a value
                .verify();
    }
}