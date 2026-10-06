@echo off
cd /d "%~dp0"

echo ============================================================
echo   Campus Placement Management System
echo ============================================================

set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java runtime not found.
    echo Please install Java 17 or Java 21, and ensure java is in your PATH
    echo or set the JAVA_HOME environment variable.
    pause
    exit /b 1
)

if not exist "target\campus-placement.jar" (
    echo [ERROR] Application binary 'target\campus-placement.jar' was not found.
    echo Please build the project first by running:
    echo   mvn clean package
    pause
    exit /b 1
)

if not exist "db.properties" (
    if not exist "src\main\resources\db.properties" (
        echo [INFO] db.properties not found.
        echo Falling back to db.properties.example defaults.
        echo To customize credentials, run: copy db.properties.example db.properties
        echo.
    )
)

"%JAVA_CMD%" -jar target\campus-placement.jar %*
if errorlevel 1 (
    echo.
    echo Application exited with code %errorlevel%.
    pause
)
