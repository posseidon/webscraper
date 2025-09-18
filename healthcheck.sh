#!/bin/sh

# Basic health check for the Spring Boot application
# This can be expanded with more sophisticated checks, e.g., checking a specific health endpoint

# Exit immediately if a command exits with a non-zero status
set -e

# Define the application URL
APP_URL="http://localhost:8080/"

# Use curl to check if the application is responding
# -f: Fail silently (no output) on server errors
# -s: Silent mode. Don't show progress meter or error messages
# -o /dev/null: Discard the output
if curl -fs -o /dev/null "$APP_URL"; then
  echo "Health check passed: Application is responsive."
  exit 0
else
  echo "Health check failed: Application is not responsive."
  exit 1
fi
