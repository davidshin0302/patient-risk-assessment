package com.abernathyclinic.patientriskassessment.dto.clinicrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRecordDTO {
    private String id;
    private String patId;
    private List<ClinicalNoteDTO> clinicalNote;
}
