package com.example.notes_service.api;

import com.example.notes_service.model.Note;
import com.example.notes_service.repository.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/notes")
public class NoteController {

    private final NoteRepository repo;

    public NoteController(NoteRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Note> list(@PathVariable String patientId) {
        return repo.findByPatientIdOrderByDateDesc(patientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note add(@PathVariable String patientId, @RequestBody CreateNoteRequest request) {
        Note note = new Note();
        note.setId(null);
        note.setPatientId(patientId);
        note.setDate(Instant.now());
        note.setPhysician(request.getPhysician());
        note.setNote(request.getNote()); // preserves line breaks as-is
        return repo.save(note);
    }
}
