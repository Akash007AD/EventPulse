@echo off
echo =====================================================================
echo  EventPulse - Docker Compose Stack Launcher
echo =====================================================================
cd /d "%~dp0.."

where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not installed or not in PATH!
    echo Please install Docker Desktop or run services locally using scripts\start-local-services.ps1.
    pause
    exit /b 1
)

echo Packaging services before container build...
call "%~dp0build-all.bat"

echo Starting 15 Docker containers (Infrastructure + Observability + Services)...
docker compose up -d --build

echo =====================================================================
echo  EventPulse Stack is spinning up!
echo   - API Gateway:          http://localhost:8080
echo   - Keycloak OIDC:        http://localhost:8180
echo   - Grafana Dashboard:    http://localhost:3000 (admin / admin)
echo   - Prometheus Metrics:   http://localhost:9090
echo   - Zipkin Traces:        http://localhost:9411
echo   - RabbitMQ Management:  http://localhost:15672 (guest / guest)
echo =====================================================================
pause
