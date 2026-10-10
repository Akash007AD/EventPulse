@echo off
echo =====================================================================
echo  EventPulse - Docker Compose Stack Shutdown
echo =====================================================================
cd /d "%~dp0.."
docker compose down
echo [DONE] All EventPulse containers stopped and networks removed.
pause
