Diabetes Risk Assessment System

Overview
This project is a microservices-based application used to assess the risk of early-onset diabetes for patients based on:

Demographic data (age, sex)
Physician notes containing medical trigger terms

The application provides a UI-driven workflow where users can:

Log in
View existing patients
Add new patients
Add physician notes
Automatically assess diabetes risk

Architecture

The system consists of the following services:

Gateway Service (Spring Cloud Gateway + Security)
Patient Service (Spring Boot + H2 Database)
Notes Service (Spring Boot + MongoDB)
Risk Service (Spring Boot)
UI Service (Static HTML + Nginx)
Dockerized environment

All services communicate through the API Gateway.

Prerequisites

Docker & Docker Desktop
Java 17+
Maven

Running the Application

From the project root, run:

./run-all.ps1

This script will:

Start MongoDB
Start all microservices
Start the API Gateway
Start the UI

Access the Application

Open in browser:

http://localhost:4200

Authentication

Username: demo
Password: demo

Patient Workflow (UI)

After logging in:

View Patients

Patients are preloaded in the H2 database
Displayed automatically in the UI

Add Patient

Fill out the patient form in the UI
Data is stored in the H2 database

Add Notes

Add notes for a selected patient
Notes are stored in MongoDB

Risk Assessment

Risk is calculated automatically
Based on:
Patient age
Gender
Trigger terms in notes

Risk Levels

None
Borderline
In Danger
Early Onset

Data Persistence

Patient data → H2 database (initialized via data.sql)
Notes data → MongoDB
Data persists during runtime

H2 Database Access

URL:
http://localhost:8081/h2-console

Connection Settings:
JDBC URL: jdbc:h2:mem:diabetes_risk_db
Username: sa
Password: (leave empty)

Gateway Routes

/api/patients/** → patient-service
/api/patients/*/notes/** → notes-service
/api/assess/** → risk-service

Health Check

http://localhost:8080/actuator/health

Typical Workflow

Run ./run-all.ps1
Open UI → http://localhost:4200
Log in (demo/demo)
View existing patients
Add a new patient
Add notes
View calculated risk
Stop services with ./stop-all.ps1

Key Features

Microservices architecture
API Gateway with authentication
Hybrid database approach (H2 + MongoDB)
UI-driven interaction (no manual API calls required)
Docker-based setup
Automated risk calculation logic
