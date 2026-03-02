# Organize It Backend

Spring Boot backend for managing tasks and task groups with MongoDB.

## Tech Stack
- Java 21
- Spring Boot 3
- Spring Data MongoDB
- Spring Security
- Maven Wrapper

## Prerequisites
- JDK 21+
- MongoDB Atlas (or MongoDB instance)

## Environment Setup
1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
2. Update values in `.env`:
   - `SPRING_DATA_MONGODB_URI`
   - `SPRING_SECURITY_USER_NAME`
   - `SPRING_SECURITY_USER_PASSWORD`
3. Never commit `.env` (already ignored by `.gitignore`).

## Run Locally
```bash
./mvnw spring-boot:run
```

## Test
```bash
./mvnw test
```

## API Base Paths
- `/tasks`
- `/task-groups`

## Notes
- Configuration is loaded from environment variables via `src/main/resources/application.properties`.
- Before making this repository public, rotate any previously exposed credentials and enable GitHub secret scanning.
