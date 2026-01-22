#!/bin/bash

# Setup script for Java 25 Features Demo Project
# This script helps set up the development environment

set -e

echo "========================================="
echo "  Java 25 Features Demo - Setup Script"
echo "========================================="
echo ""

# Check if SDKMAN is installed
if [ ! -d "$HOME/.sdkman" ]; then
    echo "⚠️  SDKMAN not found!"
    echo ""
    echo "To install SDKMAN, run:"
    echo "  curl -s \"https://get.sdkman.io\" | bash"
    echo ""
    echo "After installation, restart your terminal and run this script again."
    exit 1
fi

# Source SDKMAN
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

echo "✓ SDKMAN detected"
echo ""

# Check if Java 25 is available
echo "Checking for Java 25..."
if ! sdk list java | grep -q "25.*open"; then
    echo "⚠️  Java 25 not found in SDKMAN repository"
    echo "Available Java 25 versions:"
    sdk list java | grep "25" || echo "  No Java 25 versions available yet"
    echo ""
    echo "You may need to:"
    echo "1. Update SDKMAN: sdk update"
    echo "2. Install Java 25 manually from: https://jdk.java.net/25/"
else
    echo "✓ Java 25 available in SDKMAN"
fi

echo ""
echo "Installing Java 25 from .sdkmanrc..."
sdk env install

echo ""
echo "Activating Java 25..."
sdk env

echo ""
echo "Current Java version:"
java -version

echo ""
echo "========================================="
echo "  Setup Options"
echo "========================================="
echo ""
echo "1. Enable SDKMAN auto-env (recommended)"
echo "   This will automatically switch to Java 25 when entering this directory"
echo ""
read -p "Enable SDKMAN auto-env? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    sdk config auto-env true
    echo "✓ SDKMAN auto-env enabled"
else
    echo "⊘ SDKMAN auto-env not enabled"
    echo "  You'll need to run 'sdk env' manually when entering this directory"
fi

echo ""
echo "========================================="
echo "  Build Project"
echo "========================================="
echo ""
read -p "Build the project now? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Building with Maven..."
    mvn clean install
    echo ""
    echo "✓ Build complete!"
else
    echo "⊘ Skipping build"
    echo "  You can build later with: mvn clean install"
fi

echo ""
echo "========================================="
echo "  Setup Complete!"
echo "========================================="
echo ""
echo "To run the application:"
echo "  mvn spring-boot:run"
echo ""
echo "The API will be available at:"
echo "  http://localhost:8080/api/java25"
echo ""
echo "Happy coding with Java 25! 🚀"
