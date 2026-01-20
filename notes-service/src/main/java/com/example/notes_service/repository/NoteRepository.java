package com.example.notes_service.repository;

import com.example.notes_service.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByPatientIdOrderByDateDesc(String patientId);
}
