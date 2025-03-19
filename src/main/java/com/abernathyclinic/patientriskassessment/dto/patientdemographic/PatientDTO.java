package com.abernathyclinic.patientriskassessment.dto.patientdemographic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
    private Long id;
    private String givenName;
    private String familyName;
    private String dateOfBirth;
    private String sex;
    private String homeAddress;
    private String phoneNumber;
}
