package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PatientRiskAssessmentService {
    @Autowired
    private PatientDemographicsApiClient patientDemographicsApiClient;
    @Autowired
    private PatientRecordClient patientRecordClient;

    public Mono<PatientRecordDTO> getPatientRecord(String patId) {
        return patientRecordClient.fetchPatientRecords(patId)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        log.warn("Unable to find Patient Record by patId: {}", patId);
                    } else {
                        log.error("Error fetching patient record: {}, Status Code: {}, Response Body: {}", patId, ex.getStatusCode(), ex.getResponseBodyAsString());
                    }
                    return Mono.empty();
                });
    }

    public String extractPatientName(ClinicalNoteDTO clinicalNoteDTO) {
        String prefix = "patient: ";
        String patientLastName = "";
        String note = clinicalNoteDTO.getNote();
        int startIndex = note.toLowerCase().indexOf(prefix);

        if (startIndex != -1) {
            startIndex += prefix.length(); // Move index to end of prefix
            int endIndex = note.toLowerCase().indexOf(" ", startIndex); // Find end index after finding patient lastName.

            if (endIndex != -1) {
                patientLastName = note.substring(startIndex, endIndex);
                log.info("patientLastName: {}", patientLastName);
            }
        }
        return patientLastName;
    }

    public PatientDTO findPatientFromPatientList(PatientListDTO patientListDTO, String lastName) {
        log.info("PatientList: {}", patientListDTO);
        log.info("patient found last name: {}", lastName);
        PatientDTO patientDTO = null;

        for (PatientDTO patient : patientListDTO.getPatientList()) {
            if (patient.getFamilyName().equalsIgnoreCase(lastName)) {
                patientDTO = patient;
            }
        }
        return patientDTO;
    }

    public Mono<PatientDTO> getPatientRiskAssessment(String patId) {
        return getPatientRecord(patId)
                .flatMap(patientRecordDTO -> {
                    Mono<PatientListDTO> patientListDTO = patientDemographicsApiClient.fetchPatientDemoGraphicData();

                    return Mono.zip(patientListDTO, Mono.just(patientRecordDTO), (tuple1, tuple2) -> {
                        PatientDTO patientDTO = findPatientFromPatientList(tuple1, extractPatientName(tuple2.getClinicalNotes().getFirst()));
                        log.info("PatintDTO: {} ", patientDTO);
                        return patientDTO;
                    });
                });
    }
}
