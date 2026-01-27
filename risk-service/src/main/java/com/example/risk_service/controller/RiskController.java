package com.example.risk_service.controller;

import com.example.risk_service.dto.RiskResponse;
import com.example.risk_service.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assess")
public class RiskController {
    private final RiskAssessmentService riskAssessmentService;

    public RiskController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<RiskResponse> assessRisk(@PathVariable String patientId) {
        String riskLevel = riskAssessmentService.assessRisk(patientId);
        return ResponseEntity.ok(new RiskResponse(patientId, riskLevel));
    }
}

