package com.example.patient_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

public class Patient {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    // Sprint 3 needs these (and your POST already sends them)
    private String dob;      // "YYYY-MM-DD"
    private String sex;      // "M" / "F"
    private String address;
    private String phone;

    public Patient() {}

    public Patient(String firstName, String lastName, String dob, String sex, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.sex = sex;
        this.address = address;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public String getSex() { return sex; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setSex(String sex) { this.sex = sex; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
}
