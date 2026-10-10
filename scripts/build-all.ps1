# EventPulse - PowerShell Multi-Service Packaging Script
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " EventPulse - Packaging All Microservices into Executable JARs" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

$rootDir = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $rootDir "backend"
Push-Location $backendDir

try {
    .\mvnw.cmd clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "=====================================================================" -ForegroundColor Green
        Write-Host " [SUCCESS] All 7 EventPulse microservices packaged successfully!" -ForegroundColor Green
        Write-Host "=====================================================================" -ForegroundColor Green
    } else {
        Write-Error "Maven build failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}
