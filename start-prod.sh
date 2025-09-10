#!/bin/bash

# Production startup script for Quiz Application
# Optimized for minimal memory usage and fast startup

echo "🚀 Starting Quiz Application in Production Mode (Undertow Server)..."

# Set production environment variables
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8080

# JVM optimization flags
JVM_OPTS="-server"
JVM_OPTS="$JVM_OPTS -Xms64m"           # Initial heap size
JVM_OPTS="$JVM_OPTS -Xmx256m"         # Maximum heap size
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"     # Use G1 garbage collector
JVM_OPTS="$JVM_OPTS -XX:+UseStringDeduplication"  # Reduce memory usage
JVM_OPTS="$JVM_OPTS -XX:+OptimizeStringConcat"    # Optimize string operations
JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops"       # Use compressed object pointers
JVM_OPTS="$JVM_OPTS -Djava.awt.headless=true"     # Headless mode for servers

# Application optimization
APP_OPTS="--spring.profiles.active=prod"
APP_OPTS="$APP_OPTS --server.compression.enabled=true"
APP_OPTS="$APP_OPTS --spring.thymeleaf.cache=true"
APP_OPTS="$APP_OPTS --logging.level.org.hibernate.SQL=WARN"

# Find the JAR file
JAR_FILE=$(find target -name "quizme-*.jar" | head -1)

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found. Please run 'mvn clean package' first."
    exit 1
fi

echo "📦 Using JAR: $JAR_FILE"
echo "🔧 JVM Options: $JVM_OPTS"
echo "⚙️  App Options: $APP_OPTS"

# Start the application
java $JVM_OPTS -jar "$JAR_FILE" $APP_OPTS

echo "✅ Quiz Application started successfully!"