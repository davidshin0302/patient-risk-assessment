package com.abernathyclinic.patientriskassessment.dto.clinicrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClinicalNoteDTO {
    private LocalDate date;
    private String note;
}
