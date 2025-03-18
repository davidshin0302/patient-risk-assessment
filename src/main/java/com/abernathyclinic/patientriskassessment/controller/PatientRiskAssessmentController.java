package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.service.PatientDemographicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private PatientDemographicsApiClient patientDemoGraphicsApiClient;
    @Autowired
    private PatientRiskAssessmentService patientRiskAssessmentService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PatientRisk>> getPatientRiskAssessmentById(@PathVariable(name = "id") String patId) {
        log.info("Processing assess/{} request.", patId);
        return patientRiskAssessmentService.getPatientRiskAssessment(patId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("The Id is not found: {}", patId);
                    return Mono.just(ResponseEntity.notFound().build());
                }))
                .onErrorResume(ex -> {
                    log.error("Internal Server Error: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
