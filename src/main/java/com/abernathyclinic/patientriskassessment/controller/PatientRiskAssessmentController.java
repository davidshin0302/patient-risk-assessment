package com.abernathyclinic.patientriskassessment.controller;

import com.abernathyclinic.patientriskassessment.service.PatientDemoGraphicsApiClient;
import com.abernathyclinic.patientriskassessment.service.PatientRecordClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assess")
public class PatientRiskAssessmentController {
    @Autowired
    private PatientRecordClient patientRecordClient;
    @Autowired
    private PatientDemoGraphicsApiClient patientDemoGraphicsApiClient;

    @GetMapping("/{id}")
    public String getPatientRiskAssessment(@PathVariable Long id){
        return "Patient: Test TestNone (age 52) diabetes assessment is: None";
    }
}
