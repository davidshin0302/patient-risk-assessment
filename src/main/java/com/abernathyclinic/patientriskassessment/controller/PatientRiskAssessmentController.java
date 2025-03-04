package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import com.abernathyclinic.patientriskassessment.service.PatientDemographicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import com.abernathyclinic.patientriskassessment.service.PatientRiskAssessmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
    public Mono<String> getPatientRiskAssessment(@Valid @PathVariable String id, @Valid @RequestParam  String patId) {
        return patientRiskAssessmentService.getAssessmentById(patId, id);
    }
}
