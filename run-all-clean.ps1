Write-Host "=== Diabetes Microservices: RUN ALL ===" -ForegroundColor Cyan

# Always run from the script folder (repo root where this .ps1 lives)
Set-Location $PSScriptRoot

# ---------------- CONFIG ----------------
$NETWORK = "diabetes-net"

$MONGO_CONTAINER = "mongo"
$MONGO_IMAGE     = "mongo:7"
$MONGO_PORT      = 27017
$MONGO_URI_DOCKER = "mongodb://mongo:27017/diabetes_risk_db"

$PATIENT_DIR       = Join-Path $PSScriptRoot "patient-service"
$PATIENT_CONTAINER = "patient-service"
$PATIENT_IMAGE     = "patient-service:1.0"
$PATIENT_PORT      = 8081

$NOTES_DIR       = Join-Path $PSScriptRoot "notes-service"
$NOTES_CONTAINER = "notes-service"
$NOTES_IMAGE     = "notes-service:1.0"
$NOTES_PORT      = 8082

$GATEWAY_DIR       = Join-Path $PSScriptRoot "gateway-service"
$GATEWAY_CONTAINER = "gateway-service"
$GATEWAY_IMAGE     = "gateway-service:1.0"
$GATEWAY_PORT      = 8080

$UI_DIR       = Join-Path $PSScriptRoot "ui-service"
$UI_CONTAINER = "ui-service"
$UI_IMAGE     = "ui-service:1.0"
$UI_PORT      = 4200
# ----------------------------------------

function Ensure-DockerRunning {
    docker version | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker is not running. Start Docker Desktop and try again."
    }
}

function Ensure-Network($name) {
    docker network inspect $name *> $null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Creating Docker network: $name"
        docker network create $name | Out-Null
    } else {
        Write-Host "Docker network exists: $name"
    }
}

function Remove-ContainerIfExists($name) {
    docker rm -f $name *> $null
}

function Maven-Package($projectDir) {
    if (!(Test-Path $projectDir)) { throw "Project folder not found: $projectDir" }

    Push-Location $projectDir
    try {
        Write-Host ""
        Write-Host "Maven build in: $projectDir" -ForegroundColor Yellow

        if (Test-Path ".\mvnw.cmd") {
            .\mvnw.cmd clean package -DskipTests
        } elseif (Test-Path ".\mvnw") {
            .\mvnw clean package -DskipTests
        } else {
            $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
            if ($null -eq $mvnCmd) { throw "Neither mvnw nor mvn found in PATH." }
            mvn clean package -DskipTests
        }

        if ($LASTEXITCODE -ne 0) { throw "Maven build failed in: $projectDir" }
    } finally {
        Pop-Location
    }
}

function Docker-Build($projectDir, $imageName) {
    if (!(Test-Path $projectDir)) { throw "Project folder not found: $projectDir" }

    Push-Location $projectDir
    try {
        Write-Host ""
        Write-Host "Docker build: $imageName" -ForegroundColor Yellow
        docker build --no-cache -t $imageName .
        if ($LASTEXITCODE -ne 0) { throw "Docker build failed for: $imageName" }
    } finally {
        Pop-Location
    }
}

function Wait-ForMongo {
    Write-Host ""
    Write-Host "Waiting for MongoDB to be ready..." -ForegroundColor DarkGray
    for ($i = 1; $i -le 30; $i++) {
        docker exec $MONGO_CONTAINER mongosh --eval "db.runCommand({ ping: 1 })" > $null 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "MongoDB is ready." -ForegroundColor DarkGray
            return
        }
        Start-Sleep -Seconds 1
    }
    throw "MongoDB did not become ready in time."
}

try {
    Ensure-DockerRunning
    Ensure-Network $NETWORK

    Write-Host ""
    Write-Host "Removing old containers (if any)..." -ForegroundColor DarkGray
    Remove-ContainerIfExists $UI_CONTAINER
    Remove-ContainerIfExists $GATEWAY_CONTAINER
    Remove-ContainerIfExists $NOTES_CONTAINER
    Remove-ContainerIfExists $PATIENT_CONTAINER
    Remove-ContainerIfExists $MONGO_CONTAINER

    # Build JARs
    Maven-Package $PATIENT_DIR
    Maven-Package $NOTES_DIR
    Maven-Package $GATEWAY_DIR

    # Docker builds
    Docker-Build  $PATIENT_DIR $PATIENT_IMAGE
    Docker-Build  $NOTES_DIR   $NOTES_IMAGE
    Docker-Build  $GATEWAY_DIR $GATEWAY_IMAGE
    Docker-Build  $UI_DIR      $UI_IMAGE

    # Start MongoDB
    Write-Host ""
    Write-Host "Starting MongoDB..." -ForegroundColor Green
    docker run -d --name $MONGO_CONTAINER --network $NETWORK -p "$MONGO_PORT`:$MONGO_PORT" $MONGO_IMAGE | Out-Null
    Wait-ForMongo

    # Start patient-service
    Write-Host ""
    Write-Host "Starting patient-service..." -ForegroundColor Green
    docker run -d --name $PATIENT_CONTAINER --network $NETWORK -p "$PATIENT_PORT`:$PATIENT_PORT" `
        -e "SPRING_DATA_MONGODB_URI=$MONGO_URI_DOCKER" `
        $PATIENT_IMAGE | Out-Null

    # Start notes-service
    Write-Host ""
    Write-Host "Starting notes-service..." -ForegroundColor Green
    docker run -d --name $NOTES_CONTAINER --network $NETWORK -p "$NOTES_PORT`:$NOTES_PORT" `
        -e "SPRING_DATA_MONGODB_URI=$MONGO_URI_DOCKER" `
        $NOTES_IMAGE | Out-Null

    # Start gateway-service
    Write-Host ""
    Write-Host "Starting gateway-service..." -ForegroundColor Green
    docker run -d --name $GATEWAY_CONTAINER --network $NETWORK -p "$GATEWAY_PORT`:$GATEWAY_PORT" `
        $GATEWAY_IMAGE | Out-Null

    # Start ui-service (nginx)
    Write-Host ""
    Write-Host "Starting ui-service..." -ForegroundColor Green
    docker run -d --name $UI_CONTAINER --network $NETWORK -p "$UI_PORT`:80" `
        $UI_IMAGE | Out-Null

    Write-Host ""
    Write-Host "All services are running" -ForegroundColor Green
    Write-Host ""
    Write-Host "UI:             http://localhost:$UI_PORT"
    Write-Host "Gateway health: http://localhost:$GATEWAY_PORT/actuator/health"
    Write-Host "Patient health: http://localhost:$PATIENT_PORT/actuator/health"
    Write-Host "Notes health:   http://localhost:$NOTES_PORT/actuator/health"
    Write-Host ""
    Write-Host "Sanity check:   curl.exe -i http://localhost:$GATEWAY_PORT/api/patients"
    Write-Host "Notes check:    curl.exe -i http://localhost:$GATEWAY_PORT/api/patients/<PATIENT_ID>/notes"
}
catch {
    Write-Host ""
    Write-Error $_
    exit 1
}
