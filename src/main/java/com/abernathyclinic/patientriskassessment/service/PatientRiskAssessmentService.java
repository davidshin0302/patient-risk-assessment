package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientDiabetesAssessment;
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

    public String extractPatientName(String note) {
        String prefix = "patient: ";

        int startIndex = note.toLowerCase().indexOf(prefix);
        int endIndex;

        if (startIndex != -1) {
            startIndex += prefix.length();
            endIndex = note.indexOf(" ", startIndex);

            if (endIndex != -1) {
                return note.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    public Mono<PatientDiabetesAssessment> getPatientRiskAssessment(String patId) {
        return getPatientRecord(patId)
                .flatMap(patientRecordDTO -> {
                    Mono<PatientListDTO> patientListDTO = patientDemographicsApiClient.fetchPatientDemoGraphicData();

                    return Mono.zip(patientListDTO, Mono.just(patientRecordDTO), (tuple1, tuple2) -> {

                        return
                    })
                })
    }
}
