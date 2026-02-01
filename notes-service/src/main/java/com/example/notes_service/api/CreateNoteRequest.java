package com.example.notes_service.api;

import jakarta.validation.constraints.NotBlank;

public class CreateNoteRequest {
    @NotBlank
    private String physician;

    @NotBlank
    private String note;

    public CreateNoteRequest() {}

    public String getPhysician() { return physician; }
    public String getNote() { return note; }

    public void setPhysician(String physician) { this.physician = physician; }
    public void setNote(String note) { this.note = note; }
}
