package com.example.notes_service.api;

public class CreateNoteRequest {
    private String physician;
    private String note;

    public CreateNoteRequest() {}

    public String getPhysician() { return physician; }
    public String getNote() { return note; }

    public void setPhysician(String physician) { this.physician = physician; }
    public void setNote(String note) { this.note = note; }
}
