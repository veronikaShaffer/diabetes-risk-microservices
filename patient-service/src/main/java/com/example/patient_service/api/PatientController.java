package com.example.patient_service.api;

import com.example.patient_service.model.Patient;
import com.example.patient_service.repository.PatientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository repo;

    public PatientController(PatientRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Patient create(@RequestBody Patient patient) {
        patient.setId(null); //  Mongo generates id
        return repo.save(patient);
    }

    @GetMapping
    public List<Patient> list() {
        return repo.findAll();
    }
}