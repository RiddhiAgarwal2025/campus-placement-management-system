#!/usr/bin/env bash
# ==============================================================================
# Campus Placement Management System - Unix/Linux/macOS Launcher
# ==============================================================================
set -e

# Change directory to script directory
cd "$(dirname "$0")"

echo "============================================================"
echo "  Campus Placement Management System"
echo "============================================================"

# 1. Locate Java
JAVA_CMD="java"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

if ! command -v "$JAVA_CMD" >/dev/null 2>&1; then
    echo "[ERROR] Java runtime not found."
    echo "Please install Java 17 or Java 21 (LTS) and ensure 'java' is in PATH or set JAVA_HOME."
    exit 1
fi

# 2. Check if the JAR exists
if [ ! -f "target/campus-placement.jar" ]; then
    echo "[ERROR] Application binary 'target/campus-placement.jar' not found."
    echo "Please package the project first by running:"
    echo ""
    echo "  mvn clean package"
    echo ""
    exit 1
fi

# 3. Inform about configuration file if missing
if [ ! -f "db.properties" ] && [ ! -f "src/main/resources/db.properties" ]; then
    echo "[INFO] 'db.properties' not found."
    echo "Falling back to 'db.properties.example' defaults."
    echo "To customize database settings, run: cp db.properties.example db.properties"
    echo ""
fi

# 4. Launch the application
exec "$JAVA_CMD" -jar target/campus-placement.jar "$@"
