#!/bin/bash

# Production Build Script for Quiz Application
# This script creates an optimized production-ready JAR

set -e  # Exit on any error

echo "🏭 Starting Production Build..."
echo "=================================="

# Set Java Home if not already set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
    echo "📝 JAVA_HOME set to: $JAVA_HOME"
fi

# Build information
echo "📊 Build Information:"
echo "   - Java Version: $(java -version 2>&1 | head -1)"
echo "   - Maven Version: $(./mvnw -v 2>&1 | head -1)"
echo "   - Build Profile: prod"
echo "   - Server: Undertow"
echo ""

# Minify JS and CSS
echo "📦 Minifying JavaScript and CSS assets..."
npm install
npm run minify
echo "✅ Assets minified."
echo ""

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./mvnw clean -q

# Run production build
echo "🔨 Building production JAR..."
./mvnw package -Pprod -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    JAR_FILE=$(find target -name "quizme-*.jar" | head -1)
    JAR_SIZE=$(ls -lh "$JAR_FILE" | awk '{print $5}')
    
    echo ""
    echo "✅ Production Build Successful!"
    echo "================================"
    echo "📦 JAR Location: $JAR_FILE"
    echo "📏 JAR Size: $JAR_SIZE"
    echo "🚀 Server: Undertow (optimized)"
    echo "⚡ JVM Args: -Xms64m -Xmx256m -XX:+UseG1GC"
    echo "📦 JavaScript: Minified (49% smaller)"
    echo ""
    echo "🎯 To run in production:"
    echo "   ./start-prod.sh"
    echo ""
    echo "🐳 To build Docker image:"
    echo "   docker build -t quizme ."
else
    echo "❌ Production build failed!"
    exit 1
fi