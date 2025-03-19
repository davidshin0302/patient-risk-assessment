package com.abernathyclinic.patientriskassessment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRisk {
    private String firstName;
    private String lastName;
    private String age;
    private String riskLevel;

    @Override
    public String toString() {
        return "Patient: " + firstName + " " + lastName + " (age " + age + ") "
                + "diabetes assessment is: " + riskLevel;
    }
}
