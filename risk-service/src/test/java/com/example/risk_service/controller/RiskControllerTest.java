package com.example.risk_service.controller;

import com.example.risk_service.service.RiskAssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskController.class)
class RiskControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    RiskAssessmentService service;

    @Test
    void assess_shouldReturnJsonWithRisk() throws Exception {
        when(service.assessRisk("p1")).thenReturn("None");

        mvc.perform(get("/api/assess/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value("p1"))
                .andExpect(jsonPath("$.riskLevel").value("None"));
    }
}
