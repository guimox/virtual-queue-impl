# Virtual Ticket Queue System

This is a Spring Boot project for a virtual ticket queue system using Kafka and Redis. It provides REST endpoints for queueing, status, and ticket purchase, and is ready for Docker Compose-based development and production deployments.

## Development Setup

1. Make sure you have Docker and Docker Compose installed.
2. Start Kafka, Redis, and Zookeeper for development:

   ```sh
   docker-compose up -d
   ```

3. Build and run the Spring Boot application:

   ```sh
   ./mvnw spring-boot:run
   ```

## Production Deployment

1. Build your Spring Boot application Docker image and push to your registry (update `docker-compose.prd.yml` with your image).
2. Start all services for production:

   ```sh
   docker-compose -f docker-compose.prd.yml up -d
   ```

## REST Endpoints

- `POST /api/queue/join?userId=...` — Join the queue
- `GET /api/queue/status?userId=...` — Get queue status
- `POST /api/queue/purchase?userId=...` — Purchase ticket (if eligible)

## Notes

- Kafka and Redis configs are in `src/main/resources/application.properties`.
- Replace the placeholder Docker image in `docker-compose.prd.yml` with your built image.
