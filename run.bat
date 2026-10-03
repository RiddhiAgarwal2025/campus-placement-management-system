@echo off
cd /d "%~dp0"
echo ============================================================
echo Starting Campus Placement Management System...
echo ============================================================
set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
)

"%JAVA_CMD%" -jar target\campus-placement.jar
if %errorlevel% neq 0 (
    echo.
    echo Application exited with code %errorlevel%.
    pause
)
