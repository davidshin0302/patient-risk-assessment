package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.service.PatientDemoGraphicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/assess")
public class PatientRiskAssessmentController {
    @Autowired
    private PatientRecordClient patientRecordClient;
    @Autowired
    private PatientDemoGraphicsApiClient patientDemoGraphicsApiClient;

    @GetMapping("/{id}")
    public Mono<String> getPatientRiskAssessment(@PathVariable String id) {
//        patientRiskAssessmentService = new PatientRiskAssessmentService(patientRecordClient.fetchPatientRecords(), patientDemoGraphicsApiClient.fetchPatientDemoGraphicData());
        patientRecordClient.fetchPatientRecords();
        patientDemoGraphicsApiClient.fetchPatientDemoGraphicData();
        return Mono.just("Hello");
    }
}
