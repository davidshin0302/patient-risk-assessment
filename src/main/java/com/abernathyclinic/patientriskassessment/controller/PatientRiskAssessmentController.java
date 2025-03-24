package com.abernathyclinic.patientriskassessment.controller;

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

/**
 * REST controller for managing patient risk assessments.
 * This class handles requests related to retrieving patient risk assessment information.
 */
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

    /**
     * Retrieves a patient's risk assessment based on their patient ID.
     *
     * @param patId The patient ID to search for.
     * @return A Mono of ResponseEntity containing the risk assessment as a String if found, or a 404 Not Found status if not. Returns a 500 Internal Server Error if an error occurs.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<String>> getPatientRiskAssessmentById(@PathVariable(name = "id") String patId) {
        log.info("Processing assess/{} request.", patId);
        return patientRiskAssessmentService.getPatientRiskAssessment(patId)
                .map(patientRisk -> {
                    return ResponseEntity.status(HttpStatus.OK).body(patientRisk.toString());
                })
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
