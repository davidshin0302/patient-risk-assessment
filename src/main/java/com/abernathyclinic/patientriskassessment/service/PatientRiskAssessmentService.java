package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
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
    private PatientDemoGraphicsApiClient patientDemoGraphicsApiClient;
    @Autowired
    private PatientRecordClient patientRecordClient;

    public Mono<PatientRecordDTO> getPatientRecord(String patId) {
        return patientRecordClient.fetchPatientRecords(patId)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().value() == 404) {
                        log.warn("Unable to find Patient Record by patId: {}", patId);
                        return Mono.empty();
                    } else {
                        log.error("Error fetching patient record: {}", patId);
                        return Mono.error(new RuntimeException("Error at Runtime"));
                    }
                });
    }

    public void getPatientRiskAssessment(String patId) {
        Mono<PatientRecordDTO> patientRecordsDTOMono = getPatientRecord(patId);

        //I want to implement to return if getPaitentRecord is empty because I know there are no data so no point of continuing below logic.

        Mono<PatientListDTO> patientListDTOMono = patientDemoGraphicsApiClient.fetchPatientDemoGraphicData();

        Mono.zip(patientListDTOMono, patientRecordsDTOMono).map(tuple -> {
                    PatientListDTO patientListDTO = tuple.getT1();
                    PatientRecordDTO patientRecordsDTO = tuple.getT2();

                    String output = "Patient List: " + patientListDTO + " \nPatient Records: " + patientRecordsDTO;
                    return output;
                })
                .subscribe(
                        output -> System.out.println("output: " + output),
                        error -> System.err.println("Error: " + error)
                );
    }
}
