package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
public class PatientRiskAssessmentService {
    @Autowired
    private PatientDemographicsApiClient patientDemographicsApiClient;
    @Autowired
    private PatientRecordClient patientRecordClient;

    private String extractPatientName(ClinicalNoteDTO clinicalNoteDTO) {
        String prefix = "patient: ";
        String lastName = "";
        String note = clinicalNoteDTO.getNote();

        int startIndex = note.toLowerCase().indexOf(prefix);
        int endIndex;

        if (startIndex != -1) {
            startIndex += prefix.length();
            endIndex = note.indexOf(" ", startIndex);

            if (endIndex != -1) {
                lastName = note.substring(startIndex, endIndex);
                log.info("Extracted patient last name from clinical note: {}", lastName);
            } else {
                log.warn("Unable to extract patient last name: {}", lastName);
            }
        }
        return lastName;
    }

    private String ageCalculator(String birthDate) {
        LocalDate parseDate;
        Period period;
        String output = "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDate = LocalDate.now();

        try {
            parseDate = LocalDate.parse(birthDate, formatter);
            period = Period.between(parseDate, currentDate);
            output = String.valueOf(period.getYears());
        } catch (DateTimeParseException ex) {
            log.error("Error at parsing birthdate: {}", birthDate, ex);
        }

        return output;
    }

    private Mono<PatientRisk> buildPatientRisk(List<PatientDTO> patientListDTO, String lastName) {
        boolean isPatientExist = false;
        PatientRisk patientRisk;
        PatientDTO patientDTO = new PatientDTO();

        for (PatientDTO patient : patientListDTO) {
            if (patient.getFamilyName() != null && patient.getFamilyName().equalsIgnoreCase(lastName.toLowerCase())) {
                patientDTO = patient;
                isPatientExist = true;
                break;
            }
        }

        if (isPatientExist) {
            patientRisk = PatientRisk.builder()
                    .firstName(patientDTO.getGivenName() != null ? patientDTO.getGivenName() : "")
                    .lastName(patientDTO.getFamilyName() != null ? patientDTO.getFamilyName() : "")
                    .age(ageCalculator(patientDTO.getDateOfBirth()) != null ? ageCalculator(patientDTO.getDateOfBirth()) : "")
                    .build();
        } else {
            patientRisk = null;
        }
        return (patientRisk != null) ? Mono.just(patientRisk) : Mono.empty();
    }

    public Mono<PatientRisk> getPatientRiskAssessment(String patId) {
        return patientRecordClient.fetchPatientRecords(patId)
                .flatMap(patientRecordDTO -> {
                    Mono<PatientListDTO> patientListDTO = patientDemographicsApiClient.fetchPatientDemoGraphicData();

                    return Mono.zip(patientListDTO, Mono.just(patientRecordDTO), (tuple1, tuple2) -> {
                        // Early return for null or empty clinical notes
                        if (tuple2.getClinicalNotes() == null || tuple2.getClinicalNotes().isEmpty()) {
                            return Mono.<PatientRisk>empty();
                        }

                        String lastName = extractPatientName(tuple2.getClinicalNotes().getFirst());
                        Mono<PatientRisk> patientRiskMono = buildPatientRisk(tuple1.getPatientList(), lastName);

                        // Early return if no risk is found
                        return patientRiskMono;
                    }).flatMap(patientRiskMono ->
                            patientRiskMono.switchIfEmpty(Mono.empty()) // Ensures Mono.empty is returned if no risk is found
                    );
                });
    }
}
