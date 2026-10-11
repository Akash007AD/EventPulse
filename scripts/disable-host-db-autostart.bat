@echo off
:: EventPulse - Disable Windows Host MySQL and Redis Auto-Start
:: Prevents port 3306 & 6379 conflicts with Docker on laptop boot.
echo =====================================================================
echo  EventPulse - Disabling Automatic Startup for Windows MySQL & Redis
echo =====================================================================

net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] This script requires Administrator privileges.
    echo Please right-click this file and select "Run as administrator".
    pause
    exit /b 1
)

echo Configuring Windows services to Manual (demand) start...
sc.exe config MySQL80 start= demand
sc.exe config Redis start= demand

echo Stopping host services if currently running...
net stop MySQL80 >nul 2>&1
net stop Redis >nul 2>&1

echo =====================================================================
echo [SUCCESS] MySQL80 and Redis are now set to MANUAL start!
echo Windows will no longer start them automatically when your laptop boots.
echo Docker Compose can now bind ports 3306 and 6379 without conflicts.
echo =====================================================================
pause
