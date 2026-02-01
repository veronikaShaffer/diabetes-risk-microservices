package com.example.notes_service.api;

import com.example.notes_service.model.Note;
import com.example.notes_service.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotesController.class)
class NotesControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    NoteRepository repo;

    @Test
    void addNote_shouldForcePatientIdFromPath_andReturnSaved() throws Exception {
        String patientId = "p1";

        // Body tries to set patientId (controller should override it with path var)
        Note input = new Note();
        input.setPatientId("WRONG");
        input.setPhysician("Dr. Demo");
        input.setNote("hemoglobin a1c");

        Note saved = new Note();
        saved.setId("n1");
        saved.setPatientId(patientId);
        saved.setPhysician("Dr. Demo");
        saved.setNote("hemoglobin a1c");

        when(repo.save(any(Note.class))).thenReturn(saved);

        mvc.perform(post("/api/patients/{id}/notes", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("n1"))
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.physician").value("Dr. Demo"));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
    }

    @Test
    void getNotes_shouldReturnNotesForPatient() throws Exception {
        String patientId = "p1";

        Note n1 = new Note();
        n1.setId("n1");
        n1.setPatientId(patientId);
        n1.setPhysician("Dr A");
        n1.setNote("note1");
        Note n2 = new Note();
        n2.setId("n2");
        n2.setPatientId(patientId);
        n2.setPhysician("Dr B");
        n2.setNote("note2");

        when(repo.findByPatientIdOrderByDateDesc(patientId)).thenReturn(List.of(n1, n2));

        mvc.perform(get("/api/patients/{id}/notes", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("n1"));

        verify(repo).findByPatientIdOrderByDateDesc(patientId);
    }

    @Test
    void addNote_whenMissingPhysician_shouldReturn400() throws Exception {
        String patientId = "p1";

        // physician missing / blank
        String badJson = """
                  {"note":"hemoglobin a1c"}
                """;

        mvc.perform(post("/api/patients/{id}/notes", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }

    @Test
    void addNote_whenMissingNote_shouldReturn400() throws Exception {
        String patientId = "p1";

        String badJson = """
                  {"physician":"Dr. Demo","note":"   "}
                """;

        mvc.perform(post("/api/patients/{id}/notes", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }


}
