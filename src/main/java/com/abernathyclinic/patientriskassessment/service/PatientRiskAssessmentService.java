package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PatientRiskAssessmentService {
    @Autowired
    private PatientDemoGraphicsApiClient patientDemoGraphicsApiClient;
    @Autowired
    private PatientRecordClient patientRecordClient;

    public void getPatientRiskAssessment() {
        Mono<PatientListDTO> patientListDTOMono = patientDemoGraphicsApiClient.fetchPatientDemoGraphicData();
        Mono<PatientRecordsDTO> patientRecordsDTOMono = patientRecordClient.fetchPatientRecords();

        Mono.zip(patientListDTOMono, patientRecordsDTOMono).map(tuple -> {
                    PatientListDTO patientListDTO = tuple.getT1();
                    PatientRecordsDTO patientRecordsDTO = tuple.getT2();

                    String output = "Patient List: " + patientListDTO + " \nPatient Records: " + patientRecordsDTO;
                    return output;
                })
                .subscribe(
                        output -> System.out.println("output: " + output),
                        error -> System.err.println("Error: " + error)
                );
    }
}
