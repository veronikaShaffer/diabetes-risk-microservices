Diabetes Risk Assessment System:
This project is a microservices-based application used to assess the risk of early-onset diabetes for patients based on:
Demographic data (age, sex)
Physician notes containing medical trigger terms

The system consists of:
Gateway Service
Patient Service
Notes Service
Risk Service
Static UI
MongoDB
Dockerized environment
All services communicate through the API Gateway.
Prerequisites:
Docker & Docker Desktop
Java 17+
Maven
Node.js (compatible version)
PowerShell (Windows)

Running the Application: Start all services in powershell from the project root running ./run-all.ps1
This script:
Starts MongoDB
Starts all Spring Boot microservices
Starts the API Gateway
Starts the Angular UI

Ensures services are started in the correct order
Stop the application: Stop all services in powershell from the project root running ./stop-all.ps1

Health Checks(After startup, verify services are running):
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health

Security (Run once per PowerShell session): $cred = Get-Credential 
//username: demo password: demo

Patient Service
Add a Patient ( via powershell):
$body = @{
  firstName = "John"
  lastName  = "Lee"
  dob       = "1984-03-06"
  sex       = "M"
  address   = "1509 Culver St"
  phone     = "841-874-6512"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/patients" `
  -ContentType "application/json" `
  -Body $body `
  -Credential $cred
  
Verify Patient Creation : curl.exe http://localhost:8080/api/patients

Notes Service
Add a note for the patient using patientId ( via powershell):
$body = @{
  physician = "Dr. Demo"
  note      = "Patient reports dizziness and headaches."
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/patients/<PATIENT_ID>/notes" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body `
  -Credential $cred
( or can be added in UI : http://localhost:4200)
Add Notes With Trigger Terms:
$body = @{
  physician = "Dr. Demo"
  note = "hemoglobin a1c microalbumin smoking cholesterol abnormal"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/patients/<PATIENT_ID>/notes" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body `
  -Credential $cred

Risk Service
Assess Diabetes Risk via powershell:
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/assess/<PATIENT_ID>" `
  -Method Get `
  -Credential $cred
(can be seen in UI)

Possible Risk Levels
None
Borderline
In Danger
Early Onset

Gateway Routes:
in powershell: Invoke-RestMethod http://localhost:8080/actuator/gateway/routes

Expected routes:
/api/patients/** → patient-service
/api/patients/*/notes/** → notes-service
/api/assess/** → risk-service
Data Persistence

Data is stored in MongoDB
Data persists between service restarts

Test Coverage (JaCoCo 80% ++) After running tests: {name}-service\target\site\jacoco\index.html  Open index.html in a browser to view coverage.

Typical Workflow
Run run-all.ps1
Verify health endpoints
Add a patient
Add physician notes
Assess diabetes risk
View results via API or UI
Stop services with stop-all.ps1
