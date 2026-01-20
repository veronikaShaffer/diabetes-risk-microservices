package com.example.risk_service.controller;

import com.example.risk_service.dto.RiskResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assess")
public class RiskController {

    @GetMapping("/{patientId}")
    public ResponseEntity<RiskResponse> assessRisk(@PathVariable String patientId) {
        // placeholder for now
        return ResponseEntity.ok(
                new RiskResponse(patientId, "NONE")
        );
    }
}

