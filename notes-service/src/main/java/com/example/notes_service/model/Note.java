package com.example.notes_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notes")
public class Note {

    @Id
    private String id;

    private String patientId;
    private Instant date;
    private String physician;
    private String note;

    public Note() {}

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public Instant getDate() { return date; }
    public String getPhysician() { return physician; }
    public String getNote() { return note; }

    public void setId(String id) { this.id = id; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setDate(Instant date) { this.date = date; }
    public void setPhysician(String physician) { this.physician = physician; }
    public void setNote(String note) { this.note = note; }
}
