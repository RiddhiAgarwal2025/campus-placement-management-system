@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================================
echo   Campus Placement Management System
echo ============================================================

:: 1. Locate Java Runtime Environment
set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

"%JAVA_CMD%" -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java runtime not found.
    echo Please install Java 17 or Java 21 (LTS) and ensure 'java' is in your PATH
    echo or set the JAVA_HOME environment variable.
    pause
    exit /b 1
)

:: 2. Check if the application JAR has been built
if not exist "target\campus-placement.jar" (
    echo [ERROR] Application binary 'target\campus-placement.jar' was not found.
    echo Please package the project first by running:
    echo.
    echo   mvn clean package
    echo.
    pause
    exit /b 1
)

:: 3. Inform about configuration file if missing
if not exist "db.properties" (
    if not exist "src\main\resources\db.properties" (
        echo [INFO] 'db.properties' not found in root or resources directory.
        echo Falling back to 'db.properties.example' defaults.
        echo To customize credentials, run: copy db.properties.example db.properties
        echo.
    )
)

:: 4. Launch the application
"%JAVA_CMD%" -jar target\campus-placement.jar %*
set "EXIT_CODE=%errorlevel%"

if %EXIT_CODE% neq 0 (
    echo.
    echo Application exited with code %EXIT_CODE%.
    pause
)
endlocal
exit /b %EXIT_CODE%
