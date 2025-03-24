package com.abernathyclinic.patientriskassessment.dto.patientdemographic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientListDTO {
    private List<PatientDTO> patientList;
}
