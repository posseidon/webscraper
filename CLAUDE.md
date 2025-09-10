# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot quiz application that provides a web-based interface for taking interactive quizzes. The application manages quiz data, supports multiple languages, and offers an engaging user experience with timed questions and scoring.

## Key Technologies

- **Spring Boot 3.5.5** with Java 17
- **Thymeleaf** for server-side templating
- **SQLite** database with JPA/Hibernate and community dialects
- **Undertow** embedded server for high performance
- **JSON Processing**: Jackson with streaming API for memory efficiency
- **Async Processing**: Spring's `@Async` with CompletableFuture
- **Containerization**: Docker support with volume mounting

## Development Commands

### Build and Run
```bash
# Build the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=QuizmeApplicationTests

# Clean and rebuild
./mvnw clean compile
```

### Docker
```bash
# Build Docker image (requires built JAR)
docker build -t quizme .

# Run with Docker (mount data volume for SQLite)
docker run -p 8080:8080 -v $(pwd)/data:/data quizme
```

## Architecture

### Core Components

1. **QuizController** (`controller/QuizController.java`):
   - REST endpoints for quiz retrieval and management
   - Handles quiz gameplay and scoring
   - Returns quiz data as JSON for frontend consumption

2. **UploadController** (`controller/UploadController.java`):
   - Manages JSON quiz file uploads with validation
   - Supports batch quiz data import
   - Handles file upload for different quiz categories

### Database Layer
- **Entities**: Question, QuizMetadata, Topic (DTOs in `repository/dto/`)
- **Repositories**: JPA repositories for each entity
- **Database**: SQLite (configurable in application.yml)

### Services
- **QuizImportService** (`service/json/QuizImportService.java`): 
  - Streaming JSON parsing for memory efficiency
  - Bulk data persistence with batch processing
  - Async processing for file imports
- **QuizService** (`service/json/QuizService.java`):
  - Core quiz business logic
  - Quiz data management and retrieval
- **JsonDifficultyService** (`service/JsonDifficultyService.java`):
  - Handles quiz difficulty levels
  - Maps difficulty ratings to quiz metadata

## Configuration

### Database (application.yml)
- SQLite database path: `/data/quizme.db`
- Hibernate DDL: `update` mode
- SQL logging enabled

## API Endpoints

### Quiz Management
- `GET /quizzes/{title}` - Retrieve quiz by title
- `POST /upload/chemistry` - Upload JSON quiz file for chemistry topics

### Quiz Gameplay
- `GET /` - Main quiz selection page
- `GET /titles` - Quiz titles page
- `GET /quiz-play` - Quiz gameplay interface

### Application
- Default port: `8080`
- Base URL: `http://localhost:8080`

## Async Processing

The application uses Spring's `@EnableAsync` with CompletableFuture for non-blocking quiz data processing. Services use asynchronous processing for improved performance during quiz imports and data operations.

## Development Notes

- The service layer uses constructor injection
- Quiz data operations are asynchronous for better performance
- Error handling returns empty results rather than throwing exceptions
- The application is designed for containerized deployment
- Supports multilingual interface (English, Hungarian, Vietnamese)
- Uses Thymeleaf for server-side rendering with modern JavaScript for interactivity