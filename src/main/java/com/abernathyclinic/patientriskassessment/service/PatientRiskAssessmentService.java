package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.Util.TriggerTermUtil;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PatientRiskAssessmentService {
    @Autowired
    private PatientRecordClient patientRecordClient;
    @Autowired
    private PatientDemographicsApiClient patientDemoGraphicsApiClient;

    public Mono<String> getAssessmentById(String patId, String id) {

        List<ClinicalNoteDTO> clinicalNotes = new ArrayList<>();

        Mono<PatientRecordDTO> patientRecord = patientRecordClient.fetchPatientRecords(patId);

        return patientRecord.flatMap(dto -> {
            int counnRisk = 0;

            for (ClinicalNoteDTO noteDTO : dto.getClinicalNotes()) {
                counnRisk = counnRisk + TriggerTermUtil.countTriggerTerms(noteDTO.getNote());
            }
            //should return Patient: {Test} {TestNone} (age {52}) diabetes assessment is: {None}
            return Mono.just("hello");
        });
    }
}
