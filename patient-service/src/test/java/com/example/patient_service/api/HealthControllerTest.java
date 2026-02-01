package com.example.patient_service.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void health_shouldReturnOk() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
