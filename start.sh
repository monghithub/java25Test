#!/bin/bash

# Start script for Java 25 Features Demo Project

set -e

echo "========================================="
echo "  Java 25 Features Demo - Starting..."
echo "  Spring Boot 4.0 + Java 25"
echo "========================================="
echo ""

# Check if SDKMAN is available and source it
if [ -d "$HOME/.sdkman" ]; then
    export SDKMAN_DIR="$HOME/.sdkman"
    [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

    # Try to activate Java 25 if .sdkmanrc exists
    if [ -f ".sdkmanrc" ]; then
        echo "Activating Java 25 from .sdkmanrc..."
        sdk env || echo "⚠️  Could not activate Java 25 automatically"
        echo ""
    fi
fi

# Check Java version
echo "Current Java version:"
java -version
echo ""

echo "========================================="
echo "  Starting Spring Boot 4.0 Application..."
echo "========================================="
echo ""
echo "The API will be available at:"
echo "  http://localhost:8080/api/java25"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Start the application with Spring Boot 4.0 (supports Java 25)
mvn spring-boot:run
