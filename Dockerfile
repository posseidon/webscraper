FROM eclipse-temurin:17-jdk-alpine
LABEL authors="Thai_Binh_Nguyen"

# Create app directory
WORKDIR /app

# Copy JAR into container
COPY target/quizme-0.0.1-SNAPSHOT.jar app.jar

# Create a folder for SQLite DB (inside container, will be mounted outside)
RUN mkdir -p /data

ENTRYPOINT ["java", "-jar", "app.jar"]

