# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot web scraping application that fetches web content and converts it to PDF format. The application provides REST endpoints to scrape websites and return the content as downloadable PDF files.

## Key Technologies

- **Spring Boot 3.5.4** with Java 17
- **JSoup 1.17.2** for web scraping and HTML parsing
- **SQLite** database with JPA/Hibernate and community dialects
- **PDF Generation**: Apache PDFBox 2.0.30 (text-to-PDF) and OpenHTMLtoPDF 1.0.10 (HTML-to-PDF)
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
./mvnw test -Dtest=WebscraperApplicationTests

# Clean and rebuild
./mvnw clean compile
```

### Docker
```bash
# Build Docker image (requires built JAR)
docker build -t webscraper .

# Run with Docker (mount data volume for SQLite)
docker run -p 8080:8080 -v $(pwd)/data:/data webscraper
```

## Architecture

### Core Components

1. **WebScraperController** (`controller/WebScraperController.java`):
   - REST endpoints: `GET /scrape` and `POST /scrape`
   - Async PDF generation and download
   - Parameters: `url` (target website), `fileName` (PDF filename)

2. **WebScraperService** (`service/web/WebScraperService.java`):
   - Orchestrates scraping and PDF conversion
   - `scrape()`: Text extraction → Text-to-PDF
   - `scrapeHtml()`: HTML extraction → HTML-to-PDF

3. **WebScraper** (`service/web/WebScraper.java`):
   - JSoup-based web scraping with 5-second timeout
   - Thread pool for concurrent processing
   - `scrape()`: Returns text content
   - `scrapeDocument()`: Returns JSoup Document

4. **PDF Generation**:
   - **Text2Pdf**: Plain text to PDF with Unicode font support (requires DejaVuSans.ttf in `src/main/resources/fonts/`)
   - **Html2Pdf**: HTML to PDF using OpenHTMLtoPDF

### Database Layer
- **Entities**: Question, QuizMetadata, Topic (DTOs in `repository/dto/`)
- **Repositories**: JPA repositories for each entity
- **Database**: MongoDb (configurable in application.yml)

### Additional Controllers
- **QuizController** (`controller/QuizController.java`): Handles quiz retrieval endpoints
- **UploadController** (`controller/UploadController.java`): Manages JSON quiz file uploads with validation

### Services
- **QuizImportService** (`service/json/QuizImportService.java`): 
  - Streaming JSON parsing for memory efficiency
  - Bulk data persistence with batch processing
  - Async processing for file imports

## Configuration

### Database (application.yml)
- SQLite database path: `/data/webscraper.db`
- Hibernate DDL: `update` mode
- SQL logging enabled

### Font Requirements
Text2Pdf requires a Unicode TTF font at `src/main/resources/fonts/DejaVuSans.ttf` for proper text rendering.

## API Endpoints

### Web Scraping
- `GET /scrape?url={url}&fileName={filename}` - Extract text and convert to PDF
- `POST /scrape?url={url}&fileName={filename}` - Extract HTML and convert to PDF  

### Quiz Management
- `GET /quizzes/{title}` - Retrieve quiz by title
- `POST /upload/chemistry` - Upload JSON quiz file for chemistry topics

### Application
- Default port: `8080`
- Base URL: `http://localhost:8080`

## Async Processing

The application uses Spring's `@EnableAsync` with CompletableFuture for non-blocking PDF generation. WebScraper creates its own thread pool based on available CPU cores.

## Development Notes

- The service layer uses constructor injection
- All PDF operations are asynchronous 
- Error handling returns empty results rather than throwing exceptions
- The application is designed for containerized deployment