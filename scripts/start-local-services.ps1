# EventPulse - Local Multi-Service Launcher (Standalone without Docker)
$rootDir = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $rootDir "backend"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " EventPulse - Local Service Launcher (Standalone Mode)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

# Load .env variables if present
$envFile = Join-Path $backendDir ".env"
if (Test-Path $envFile) {
    Write-Host "Loading environment configurations from $envFile..." -ForegroundColor Yellow
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $name = $parts[0].Trim()
            $val = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $val, [System.EnvironmentVariableTarget]::Process)
        }
    }
}

$services = @(
    @{ Name = "Event Service"; Jar = "services\event-service\target\event-service-0.0.1-SNAPSHOT.jar"; Port = 8081 },
    @{ Name = "Booking Service"; Jar = "services\booking-service\target\booking-service-0.0.1-SNAPSHOT.jar"; Port = 8082 },
    @{ Name = "Payment Service"; Jar = "services\payment-service\target\payment-service-0.0.1-SNAPSHOT.jar"; Port = 8083 },
    @{ Name = "Notification Service"; Jar = "services\notification-service\target\notification-service-0.0.1-SNAPSHOT.jar"; Port = 8084 },
    @{ Name = "Analytics Service"; Jar = "services\analytics-service\target\analytics-service-0.0.1-SNAPSHOT.jar"; Port = 8085 },
    @{ Name = "AI Service"; Jar = "services\ai-service\target\ai-service-0.0.1-SNAPSHOT.jar"; Port = 8086 },
    @{ Name = "API Gateway"; Jar = "gateway\target\gateway-0.0.1-SNAPSHOT.jar"; Port = 8080 }
)

Write-Host "`nTo run an individual service, run:" -ForegroundColor Green
Write-Host "  java -jar backend\services\event-service\target\event-service-0.0.1-SNAPSHOT.jar`n" -ForegroundColor White

Write-Host "Service Port Registry:" -ForegroundColor Cyan
foreach ($svc in $services) {
    $fullJar = Join-Path $backendDir $svc.Jar
    $status = if (Test-Path $fullJar) { "JAR READY" } else { "NOT COMPILED (run .\scripts\build-all.ps1)" }
    Write-Host ("  {0,-22} : Port {1,-4} [{2}]" -f $svc.Name, $svc.Port, $status)
}
Write-Host "=====================================================================" -ForegroundColor Cyan
