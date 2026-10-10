@echo off
echo =====================================================================
echo  EventPulse - Multi-Service Packaging Script
echo =====================================================================
cd /d "%~dp0..\backend"
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed! Check Maven output above.
    exit /b %ERRORLEVEL%
)
echo =====================================================================
echo  [SUCCESS] All 7 EventPulse microservices packaged into fat JARs!
echo =====================================================================
pause
