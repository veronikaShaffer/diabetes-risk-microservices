package com.example.risk_service.service;

import com.example.risk_service.client.NotesClient;
import com.example.risk_service.client.PatientClient;
import com.example.risk_service.dto.NoteDto;
import com.example.risk_service.dto.PatientDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RiskAssessmentService {
    private static final Set<String> TRIGGERS = Set.of(
            "hemoglobin a1c",
            "microalbumin",
            "height",
            "weight",
            "smoking",
            "abnormal",
            "cholesterol",
            "dizziness",
            "relapse",
            "reaction",
            "antibody"
    );

    private final PatientClient patientClient;
    private final NotesClient notesClient;

    public RiskAssessmentService(PatientClient patientClient, NotesClient notesClient) {
        this.patientClient = patientClient;
        this.notesClient = notesClient;
    }

    public String assessRisk(String patientId) {
        System.out.println("ASSESS START patientId=" + patientId);

        PatientDto patient = patientClient.getPatientById(patientId);
        System.out.println("PATIENT=" + patient);

        List<NoteDto> notes = notesClient.getNotesByPatientId(patientId);
        System.out.println("NOTES size=" + (notes == null ? "null" : notes.size()));

        int age = computeAge(patient != null ? patient.getDateOfBirth() : null);
        String gender = normalizeGender(
                patient != null ? patient.getGender() : null,
                patient != null ? patient.getSex() : null
        );

        int triggerCount = countTriggers(notes);
        return mapRisk(age, gender, triggerCount);
    }

    private int computeAge(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isBlank()) return 0;

        try {
            LocalDate dob = LocalDate.parse(dateOfBirth); // expects YYYY-MM-DD
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    private String normalizeGender(String gender, String sex) {
        String g = (gender != null && !gender.isBlank()) ? gender : sex;
        if (g == null) return "";

        g = g.trim().toLowerCase(Locale.ROOT);
        if (g.startsWith("m")) return "male";
        if (g.startsWith("f")) return "female";
        return g;
    }

    private int countTriggers(List<NoteDto> notes) {
        if (notes == null || notes.isEmpty()) return 0;

        String allText = notes.stream()
                .filter(n -> n != null && n.getNote() != null)
                .map(n -> n.getNote().toLowerCase(Locale.ROOT))
                .reduce("", (a, b) -> a + "\n" + b);

        long uniqueFound = TRIGGERS.stream()
                .filter(allText::contains)
                .count();

        return (int) uniqueFound;
    }

    private String mapRisk(int age, String gender, int triggers) {
        boolean over30 = age >= 30;
        boolean under30 = age < 30;

        // Early Onset
        if (over30 && triggers >= 8) return "EarlyOnset";
        if (under30 && "male".equals(gender) && triggers >= 5) return "EarlyOnset";
        if (under30 && "female".equals(gender) && triggers >= 6) return "EarlyOnset";

        // In Danger
        if (over30 && triggers >= 6 && triggers <= 7) return "InDanger";
        if (under30 && "male".equals(gender) && triggers >= 3 && triggers <= 4) return "InDanger";
        if (under30 && "female".equals(gender) && triggers >= 4 && triggers <= 5) return "InDanger";

        // Borderline
        if (over30 && triggers >= 2 && triggers <= 5) return "Borderline";

        return "None";
    }
}
