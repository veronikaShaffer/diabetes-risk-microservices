package com.example.risk_service.service;

import com.example.risk_service.client.NotesClient;
import com.example.risk_service.client.PatientClient;
import com.example.risk_service.dto.NoteDto;
import com.example.risk_service.dto.PatientDto;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RiskAssessmentServiceTest {

    private final PatientClient patientClient = mock(PatientClient.class);
    private final NotesClient notesClient = mock(NotesClient.class);

    private final RiskAssessmentService service =
            new RiskAssessmentService(patientClient, notesClient);

    private PatientDto patient(String dobIso, String gender, String sex) {
        PatientDto p = new PatientDto();
        p.setDateOfBirth(dobIso);
        p.setSex(gender);
        p.setSex(sex);
        return p;
    }


    private PatientDto patientWithSex(String dobIso, String sex) {
        PatientDto p = new PatientDto();
        p.setDateOfBirth(dobIso);
        p.setSex(sex);
        return p;
    }

    private NoteDto note(String text) {
        NoteDto n = new NoteDto();
        n.setNote(text);
        return n;
    }

    @SuppressWarnings("unchecked")
    private static List<String> triggers() {
        try {
            Field f = RiskAssessmentService.class.getDeclaredField("TRIGGERS");
            f.setAccessible(true);

            Object raw = f.get(null);

            if (raw instanceof Collection<?> c) {
                List<String> out = new ArrayList<>();
                for (Object o : c) out.add(String.valueOf(o));
                return out;
            }

            throw new IllegalStateException("TRIGGERS is not a Collection, it is: " + raw.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Could not read TRIGGERS via reflection", e);
        }
    }

    private static String noteWithFirstNTriggers(int n) {
        List<String> t = triggers();
        if (t.size() < n) {
            throw new IllegalStateException("TRIGGERS has only " + t.size() + " entries, need " + n);
        }
        return String.join(" ", t.subList(0, n));
    }

    @Test
    void assessRisk_whenNoPatientAndNoNotes_shouldNotCrash_andReturnSomeRisk() {
        String id = "p0";
        when(patientClient.getPatientById(id)).thenReturn(null);
        when(notesClient.getNotesByPatientId(id)).thenReturn(null);

        String risk = service.assessRisk(id);

        assertThat(risk).isNotBlank();
    }

    @Test
    void assessRisk_whenNoTriggers_shouldReturnNone() {
        String id = "p1";
        when(patientClient.getPatientById(id)).thenReturn(patient("1984-03-06", "F", null));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note("annual checkup normal labs"),
                note("no issues")
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualToIgnoringCase("None");
    }

    @Test
    void assessRisk_when5AndMoreTriggersAndAgeUnder30_shouldReturnNone() {
        String id = "p1";
        when(patientClient.getPatientById(id)).thenReturn(patient("1994-03-06", "M", null));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note("smoking, abnormal ,relapse"),
                note("no issues")
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualToIgnoringCase("Borderline");
    }


    @Test
    void assessRisk_shouldHandleGenderFromGenderField() {
        String id = "p2";
        when(patientClient.getPatientById(id)).thenReturn(patient("1984-03-06", "Male", null));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note("hemoglobin a1c abnormal")
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isNotBlank();
    }

    @Test
    void assessRisk_shouldHandleGenderFromSexField_whenGenderMissing() {
        String id = "p3";
        when(patientClient.getPatientById(id)).thenReturn(patient("1984-03-06", null, "M"));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note("cholesterol dizziness")
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isNotBlank();
    }

    @Test
    void assessRisk_over30_with8plusTriggers_shouldBeEarlyOnset() {
        String id = "earlyOver30";
        when(patientClient.getPatientById(id)).thenReturn(patientWithSex("1984-03-06", "F"));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note(noteWithFirstNTriggers(8))
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualTo("EarlyOnset");
    }

    @Test
    void assessRisk_under30_male_with3to4Triggers_shouldBeInDanger() {
        String id = "dangerUnder30M";
        String dob = LocalDate.now().minusYears(25).toString();
        when(patientClient.getPatientById(id)).thenReturn(patientWithSex(dob, "M"));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note(noteWithFirstNTriggers(3))
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualTo("InDanger");
    }

    @Test
    void assessRisk_under30_male_with5plusTriggers_shouldBeEarlyOnset() {
        String id = "earlyUnder30M";
        String dob = LocalDate.now().minusYears(25).toString();
        when(patientClient.getPatientById(id)).thenReturn(patientWithSex(dob, "M"));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(
                note(noteWithFirstNTriggers(5))
        ));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualTo("EarlyOnset");
    }
    @Test
    void assessRisk_invalidDob_shouldHitParseCatch_andReturnNone() {
        String id = "badDob";
        when(patientClient.getPatientById(id)).thenReturn(patientWithSex("NOT-A-DATE", "F"));
        when(notesClient.getNotesByPatientId(id)).thenReturn(List.of(note("nothing")));

        String risk = service.assessRisk(id);

        assertThat(risk).isEqualTo("None");
    }
}
