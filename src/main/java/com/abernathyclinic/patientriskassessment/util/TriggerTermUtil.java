package com.abernathyclinic.patientriskassessment.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for counting trigger terms in clinical notes.
 * This class provides a static method to count occurrences of predefined trigger terms.
 */
public class TriggerTermUtil {
    private static final Set<String> TRIGGER_TERMS = new HashSet<>(Arrays.asList(
            "Hemoglobin A1C",
            "Microalbumin",
            "Body Height",
            "Body Weight",
            "Smoker",
            "Abnormal",
            "Cholesterol",
            "Dizziness",
            "Relapse",
            "Reaction",
            "Antibodies"
    ));

    /**
     * Counts the occurrences of trigger terms in a given text.
     *
     * @param note The text to search for trigger terms.
     * @return The number of trigger terms found in the text.
     */
    public static int countTriggerTerms(String note) {
        int totalCount = 0;

        if (note.isEmpty()) {
            return totalCount;
        }

        for (String term : TRIGGER_TERMS) {
            totalCount += StringUtils.countOccurrencesOf(note, term);
        }

        return totalCount;
    }

}
