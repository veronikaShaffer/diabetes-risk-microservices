Write-Host "=== Diabetes Microservices: STOP ALL ===" -ForegroundColor Cyan

Set-Location $PSScriptRoot

# -------- CONFIG --------
$NETWORK = "diabetes-net"

$CONTAINERS = @(
  "ui-service",
  "gateway-service",
  "notes-service",
  "patient-service",
  "mongo"
)

# Set to $true only if you want to remove the docker network too
$REMOVE_NETWORK = $false
# ------------------------

function Remove-ContainerIfExists($name) {
    docker rm -f $name *> $null
}

try {
    Write-Host "Stopping/removing containers..." -ForegroundColor Yellow
    foreach ($c in $CONTAINERS) {
        Write-Host " - $c"
        Remove-ContainerIfExists $c
    }

    if ($REMOVE_NETWORK) {
        Write-Host "Removing network: $NETWORK" -ForegroundColor Yellow
        docker network rm $NETWORK *> $null
    }

    Write-Host "Done." -ForegroundColor Green
}
catch {
    Write-Host ""
    Write-Error $_
    exit 1
}
