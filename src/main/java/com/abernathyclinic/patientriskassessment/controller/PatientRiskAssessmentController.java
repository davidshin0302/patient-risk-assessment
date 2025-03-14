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
    public ResponseEntity<Mono<PatientRisk>> getPatientRiskAssessmentById(@PathVariable(name = "id") String patId) {
        ResponseEntity<Mono<PatientRisk>> responseEntity;

        try {
            Mono<PatientRisk> patientRisk = patientRiskAssessmentService.getPatientRiskAssessment(patId);
            responseEntity = ResponseEntity.status(HttpStatus.OK).body(patientRisk);

            log.info("Processing get patent assessment by id:" + patId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error("Unable to get patient assement from the Id: {}", patId);
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return responseEntity;
    }
}
