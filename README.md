# ReleasePilot Challenge

ReleasePilot is a Java backend REST API for The Agile Monkeys technical challenge. It manages how application versions move through deployment environments with a Promotion-centered domain model.

## Documents

- [Acceptance Criteria](docs/acceptance-criteria.md)
- [Challenge Architecture](docs/challenge-architecture.md)
- [Walkthrough Motion Video Storyboard](docs/walkthrough/motion-video-storyboard.md)

## Stack

- Java 17
- Spring Boot 3
- Gradle
- PostgreSQL
- RabbitMQ
- Flyway

## Prerequisites

- Java 17
- Docker
- Docker Compose

## Run Locally

Start infrastructure:

```bash
docker compose up -d
```

Run tests:

```bash
./gradlew test
```

Start the API:

```bash
./gradlew bootRun
```

Check health:

```bash
curl http://localhost:8080/actuator/health
```

Stop infrastructure:

```bash
docker compose down
```

Reset infrastructure:

```bash
docker compose down -v
```

## Final Walkthrough

The final challenge package will include a generated motion video for challenge item `04 Walk us through it`.

Target output:

```text
deliverables/walkthrough/releasepilot-walkthrough.mp4
```
