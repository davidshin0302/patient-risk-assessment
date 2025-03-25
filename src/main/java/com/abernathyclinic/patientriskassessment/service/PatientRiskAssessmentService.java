package com.abernathyclinic.patientriskassessment.service;

import com.abernathyclinic.patientriskassessment.dto.clinicrecord.ClinicalNoteDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordDTO;
import com.abernathyclinic.patientriskassessment.dto.clinicrecord.PatientRecordsDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientDTO;
import com.abernathyclinic.patientriskassessment.dto.patientdemographic.PatientListDTO;
import com.abernathyclinic.patientriskassessment.model.PatientRisk;
import com.abernathyclinic.patientriskassessment.util.TriggerTermUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Service class for assessing patient risk.
 * This class handles the logic for retrieving and calculating patient risk assessments.
 */
@Slf4j
@Service
public class PatientRiskAssessmentService {
    @Autowired
    private PatientDemographicsApiClient patientDemographicsApiClient;
    @Autowired
    private PatientRecordClient patientRecordClient;

    /**
     * Finds a patient record within a list of records by family name.
     *
     * @param patientRecordsDTO The DTO containing the list of patient records.
     * @param familyName The family name to search for.
     * @return The PatientRecordDTO matching the family name, or a new empty PatientRecordDTO if not found.
     */
    public PatientRecordDTO findPatientRecord(PatientRecordsDTO patientRecordsDTO, String familyName) {
        PatientRecordDTO output = new PatientRecordDTO();

        for (PatientRecordDTO patientRecordDTO : patientRecordsDTO.getPatientRecords()) {
            List<ClinicalNoteDTO> clinicalNotesDTO = patientRecordDTO.getClinicalNotes();

            for (ClinicalNoteDTO clinicalNoteDTO : clinicalNotesDTO) {
                String patientName = extractPatientName(clinicalNoteDTO);
                if (patientName.equalsIgnoreCase(familyName)) {
                    output = patientRecordDTO;
                    break;
                }
            }
        }
        return output;
    }


    /**
     * Extracts the patient's last name from a clinical note.
     *
     * @param clinicalNoteDTO The clinical note from which to extract the last name.
     * @return The extracted last name, or an empty string if not found.
     */
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

    /**
     * Calculates the patient's age based on their birthdate.
     *
     * @param birthDate The patient's birthdate in "yyyy-MM-dd" format.
     * @return The calculated age as a String, or an empty string if an error occurs.
     */
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

    /**
     * Determines the patient's risk level based on clinical notes and patient demographics.
     *
     * @param clinicalNotes The list of clinical notes for the patient.
     * @param patientDTO    The patient's demographic information.
     * @return The determined risk level as a String.
     */
    private String determineRiskLevel(List<ClinicalNoteDTO> clinicalNotes, PatientDTO patientDTO) {
        final String FEMALE = "f";
        final String MALE = "m";
        String result = "None";
        String gender = patientDTO.getSex().toLowerCase();
        int matchedTriggers = 0;
        int age = Integer.parseInt(ageCalculator(patientDTO.getDateOfBirth()));

        if (!clinicalNotes.isEmpty()) {
            for (ClinicalNoteDTO clinicalNote : clinicalNotes) {
                matchedTriggers += TriggerTermUtil.countTriggerTerms(clinicalNote.getNote());
            }
        }

        if (matchedTriggers == 0) {
            return "None";
        }

        if ((age < 30 && gender.equals(MALE) && matchedTriggers >= 5)
                || (age < 30 && gender.equals(FEMALE) && matchedTriggers >= 7)
                || (age >= 30 && matchedTriggers >= 8)) {
            result = "Early onset";
        } else if (((age < 30 && gender.equals(MALE) && matchedTriggers == 3)
                || (age < 30 && gender.equals(FEMALE) && matchedTriggers == 4)
                || (age >= 30 && matchedTriggers == 6))) {
            result = "In danger";
        } else if (age >= 30 && matchedTriggers == 2) {
            result = "Borderline";
        }


        return result; //patient has no doctor’s notes containing any of the trigger
    }

    /**
     * Builds a PatientRisk object based on patient demographics and clinical notes.
     *
     * @param patientListDTO The list of patient demographic data.
     * @param clinicalNotes  The list of clinical notes for the patient.
     * @param lastName       The patient's last name.
     * @return A Mono of PatientRisk, or an empty Mono if the patient is not found.
     */
    private Mono<PatientRisk> buildPatientRisk(List<PatientDTO> patientListDTO, List<ClinicalNoteDTO> clinicalNotes, String lastName) {
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
            String patientAge = ageCalculator(patientDTO.getDateOfBirth());
            String riskLevel = determineRiskLevel(clinicalNotes, patientDTO);

            patientRisk = PatientRisk.builder()
                    .firstName(patientDTO.getGivenName())
                    .lastName(patientDTO.getFamilyName())
                    .age(patientAge)
                    .riskLevel(riskLevel)
                    .build();
        } else {
            patientRisk = null;
        }
        return (patientRisk != null) ? Mono.just(patientRisk) : Mono.empty();
    }

    /**
     * Retrieves the patient's risk assessment based on their patient ID.
     *
     * @param patId The patient ID to search for.
     * @return A Mono of PatientRisk containing the risk assessment, or an empty Mono if not found or an error occurs.
     */
    public Mono<PatientRisk> getPatientRiskAssessmentById(String patId) {
        return patientRecordClient.fetchPatientRecordsById(patId)
                .flatMap(patientRecordDTO -> {
                    Mono<PatientListDTO> patientListDTO = patientDemographicsApiClient.fetchPatientDemoGraphicData();

                    return Mono.zip(patientListDTO, Mono.just(patientRecordDTO), (tuple1, tuple2) -> {
                        // Early return for null or empty clinical notes
                        if (tuple2.getClinicalNotes() == null || tuple2.getClinicalNotes().isEmpty()) {
                            return Mono.<PatientRisk>empty();
                        }

                        String lastName = extractPatientName(tuple2.getClinicalNotes().getFirst());

                        // Early return if no risk is found
                        return buildPatientRisk(tuple1.getPatientList(), tuple2.getClinicalNotes(), lastName);
                    }).flatMap(patientRiskMono ->
                            patientRiskMono.switchIfEmpty(Mono.empty()) // Ensures Mono.empty is returned if no risk is found
                    );
                });
    }

    /**
     * Retrieves the patient's risk assessment by family name.
     *
     * @param familyName The family name to search for.
     * @return A Mono of PatientRisk, or an empty Mono if not found or no risk data.
     */
    public Mono<PatientRisk> getPatientRiskAssessmentByFamilyName(String familyName) {
        return patientRecordClient.fetchAllPatientRecords()
                .flatMap(patientRecordsDTO -> {
                    Mono<PatientListDTO> patientListDTO = patientDemographicsApiClient.fetchPatientDemoGraphicData();

                    return Mono.zip(patientListDTO, Mono.just(patientRecordsDTO), (tuple1, tuple2) -> {
                        System.out.println(tuple2);
                        PatientRecordDTO patientRecordDTO = findPatientRecord(tuple2, familyName);
                        // Early return for null or empty clinical notes
                        if (patientRecordDTO.getClinicalNotes() == null || patientRecordDTO.getClinicalNotes().isEmpty()) {
                            return Mono.<PatientRisk>empty();
                        }

                        String lastName = extractPatientName(patientRecordDTO.getClinicalNotes().getFirst());

                        // Early return if no risk is found
                        return buildPatientRisk(tuple1.getPatientList(), patientRecordDTO.getClinicalNotes(), lastName);
                    }).flatMap(patientRiskMono ->
                            patientRiskMono.switchIfEmpty(Mono.empty()) // Ensures Mono.empty is returned if no risk is found
                    );
                });
    }
}
