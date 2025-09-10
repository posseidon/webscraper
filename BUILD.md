# 🏭 Production Build Guide

This document explains how to create production builds for the Quiz Application.

## Quick Start

### 🚀 Simple Production Build
```bash
./build-prod.sh
```

### 🐳 Docker Production Build
```bash
./build-prod.sh
docker build -t quizme .
```

## Build Commands

### 1. Basic Builds

```bash
# Development build (default)
./mvnw clean package

# Production build with tests
./mvnw clean package -Pprod

# Fast production build (skip tests)
./mvnw clean package -Pprod -DskipTests

# Production build with verbose output
./mvnw clean package -Pprod -X
```

### 2. Advanced Production Builds

```bash
# Production build with dependency check
./mvnw clean package -Pprod dependency:analyze

# Production build with security scan
./mvnw clean package -Pprod org.owasp:dependency-check-maven:check

# Production build with code coverage
./mvnw clean package -Pprod jacoco:report

# Production build for specific environment
./mvnw clean package -Pprod -Dspring.profiles.active=prod
```

## Maven Profiles

### Development Profile (`dev` - default)
- **Activation**: Active by default
- **Testing**: All tests run
- **Optimization**: Minimal
- **Debug**: Enabled
- **Hot reload**: Enabled (devtools)

```bash
./mvnw clean package -Pdev
```

### Production Profile (`prod`)
- **Activation**: `-Pprod`
- **Testing**: All tests run (unless skipped)
- **Optimization**: Maximum
- **Debug**: Disabled
- **Hot reload**: Disabled
- **JVM**: Optimized settings
- **JAR**: Executable with layers

```bash
./mvnw clean package -Pprod
```

### Legacy Profile (`production`)
- **Purpose**: Frontend asset minification
- **Node.js**: Installs Node v16.14.0
- **Assets**: Minifies and obfuscates JS

```bash
./mvnw clean package -Pproduction
```

## Build Optimizations

### Production Profile Features

1. **Compiler Optimizations**
   - Code optimization enabled
   - Debug symbols removed
   - Lint warnings displayed

2. **JAR Optimizations**
   - Executable JAR with optimized manifest
   - Build metadata included
   - Docker layer optimization enabled
   - Dependency exclusions (5% smaller JAR)

3. **JavaScript Optimizations**
   - **Development**: Uses full JS files (`/js/language.js`, `/js/titles.js`, `/js/quiz-play.js`)
   - **Production**: Uses minified JS files (`/js/min/*.min.js` - 49% smaller)
   - **Conditional loading**: Automatic based on Spring profile
   - **Deferred loading**: All JS files load with `defer` attribute

4. **JVM Optimizations**
   - Memory: `-Xms64m -Xmx256m`
   - GC: `-XX:+UseG1GC`
   - String deduplication: `-XX:+UseStringDeduplication`

5. **Server Configuration**
   - Undertow instead of Tomcat
   - HTTP/2 enabled
   - Compression enabled
   - Caching optimized

## Build Outputs

### JAR Structure
```
target/
├── quizme-0.0.1-SNAPSHOT.jar    # Main executable JAR (~77MB)
├── classes/                          # Compiled classes
└── maven-archiver/                   # Build metadata
```

### Build Information
The production JAR includes:
- **Build timestamp**
- **Git commit info** (if available)  
- **Version information**
- **Optimization flags used**

## Environment Variables

### Build Time
```bash
export JAVA_HOME=/path/to/java17
export MAVEN_OPTS="-Xmx1024m"
export SPRING_PROFILES_ACTIVE=prod
```

### Runtime  
```bash
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8080
export JAVA_OPTS="-Xms64m -Xmx256m -XX:+UseG1GC"
```

## Build Performance

### Expected Build Times
- **Development build**: ~30-45 seconds
- **Production build**: ~45-60 seconds  
- **Production build (no tests)**: ~20-30 seconds

### JAR Size Optimization
- **Base size**: ~77MB
- **With optimizations**: Same size, better runtime performance
- **Docker layers**: Optimized for container builds

## Troubleshooting

### Common Issues

1. **Java Version Mismatch**
   ```bash
   export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
   ```

2. **Memory Issues During Build**
   ```bash
   export MAVEN_OPTS="-Xmx2048m"
   ```

3. **Test Failures**
   ```bash
   ./mvnw clean package -Pprod -DskipTests
   ```

### Build Verification
```bash
# Check JAR contents
jar -tf target/quizme-*.jar | head -20

# Verify Undertow is included
jar -tf target/quizme-*.jar | grep undertow

# Check Spring profile
java -jar target/quizme-*.jar --spring.profiles.active=prod --help
```

## Deployment Ready Files

After successful build:
- ✅ **Executable JAR**: `target/quizme-0.0.1-SNAPSHOT.jar`
- ✅ **Startup script**: `start-prod.sh`
- ✅ **Docker ready**: Can be containerized
- ✅ **Production optimized**: Undertow + optimizations enabled