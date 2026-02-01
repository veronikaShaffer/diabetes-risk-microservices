package com.example.patient_service.api;

import com.example.patient_service.model.Patient;
import com.example.patient_service.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    PatientRepository repo;

    @Test
    void create_shouldNullOutId_andReturnSavedPatient() throws Exception {
        Patient input = new Patient("Veronika", "Shaffer", "1984-03-06", "M", "1509 Culver St", "841-874-6512");
        input.setId("SHOULD_BE_IGNORED");

        Patient saved = new Patient("Veronika", "Shaffer", "1984-03-06", "M", "1509 Culver St", "841-874-6512");
        saved.setId("generated123");

        when(repo.save(any(Patient.class))).thenReturn(saved);

        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("generated123"))
                .andExpect(jsonPath("$.firstName").value("Veronika"))
                .andExpect(jsonPath("$.lastName").value("Shaffer"));

        // ensure controller sets id = null before saving
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    void list_shouldReturnAllPatients() throws Exception {
        Patient p1 = new Patient("A", "B", "2000-01-01", "F", "addr", "111"); p1.setId("id1");
        Patient p2 = new Patient("C", "D", "1999-02-02", "M", "addr2", "222"); p2.setId("id2");

        when(repo.findAll()).thenReturn(List.of(p1, p2));

        mvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id1"))
                .andExpect(jsonPath("$[1].id").value("id2"));

        verify(repo).findAll();
    }

    @Test
    void getById_whenFound_shouldReturn200() throws Exception {
        Patient p = new Patient("Veronika", "Shaffer", "1984-03-06", "M", "1509 Culver St", "841-874-6512");
        p.setId("abc");
        when(repo.findById("abc")).thenReturn(Optional.of(p));

        mvc.perform(get("/api/patients/abc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("abc"))
                .andExpect(jsonPath("$.firstName").value("Veronika"));

        verify(repo).findById("abc");
    }

    @Test
    void getById_whenMissing_shouldReturn404() throws Exception {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        mvc.perform(get("/api/patients/missing"))
                .andExpect(status().isNotFound());

        verify(repo).findById("missing");
    }
}
