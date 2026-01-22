#!/bin/bash

# Stop script for Java 25 Features Demo Project

echo "========================================="
echo "  Java 25 Features Demo - Stopping..."
echo "========================================="
echo ""

# Find and kill the Java process
PID=$(pgrep -f "com.monghit.java25.Java25FeaturesApplication")

if [ -z "$PID" ]; then
    echo "No application is running."
else
    echo "Found application running with PID: $PID"
    echo "Stopping application..."
    kill $PID

    # Wait a bit and check if it's stopped
    sleep 2

    if pgrep -f "com.monghit.java25.Java25FeaturesApplication" > /dev/null; then
        echo "Application didn't stop gracefully, forcing shutdown..."
        kill -9 $PID
        sleep 1
    fi

    if pgrep -f "com.monghit.java25.Java25FeaturesApplication" > /dev/null; then
        echo "❌ Failed to stop application"
        exit 1
    else
        echo "✓ Application stopped successfully"
    fi
fi

echo ""
echo "========================================="
echo "  Application stopped"
echo "========================================="
