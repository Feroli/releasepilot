# ReleasePilot Challenge

ReleasePilot is a Java backend REST API for The Agile Monkeys technical challenge. It manages how application versions move through deployment environments with a Promotion-centered domain model.

## Documents

- [Acceptance Criteria](docs/acceptance-criteria.md)
- [Challenge Architecture](docs/challenge-architecture.md)
- [Walkthrough Motion Video Storyboard](docs/walkthrough/motion-video-storyboard.md)
- [Walkthrough Motion Video Script](docs/walkthrough/motion-video-script.md)
- [Manual Testing](manual-testing/README.md)

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

## API Examples

Create an application:

```bash
curl -X POST http://localhost:8080/applications \
  -H 'Content-Type: application/json' \
  -d '{"id":"payments-api","name":"Payments API"}'
```

Register a version with `DEV` completed:

```bash
curl -X POST http://localhost:8080/applications/payments-api/versions \
  -H 'Content-Type: application/json' \
  -d '{"version":"1.4.0","devCompleted":true}'
```

Request promotion:

```bash
curl -X POST http://localhost:8080/promotions \
  -H 'Content-Type: application/json' \
  -d '{"applicationId":"payments-api","version":"1.4.0","sourceEnvironment":"DEV","targetEnvironment":"STAGING","requestedBy":"fernando"}'
```

Approve, deploy, complete, roll back, and cancel commands are available at:

```text
POST /promotions/{id}/approve
POST /promotions/{id}/deployments
POST /promotions/{id}/complete
POST /promotions/{id}/rollback
POST /promotions/{id}/cancel
```

Read models:

```text
GET /promotions/{id}
GET /applications/{id}/status
GET /applications/{id}/promotions
GET /promotions/{id}/audit-log
GET /promotions/{id}/release-notes
```

## Final Walkthrough

This repository includes a generated motion video.

- Video: [`deliverables/walkthrough/releasepilot-walkthrough.mp4`](deliverables/walkthrough/releasepilot-walkthrough.mp4)
- Script (topic → scene map): [`docs/walkthrough/motion-video-script.md`](docs/walkthrough/motion-video-script.md)
- Storyboard: [`docs/walkthrough/motion-video-storyboard.md`](docs/walkthrough/motion-video-storyboard.md)

It is a ~3:28, 1080p, full-motion piece with a synthetic voiceover and burned-in subtitles that
covers the problem framing, the Promotion aggregate, the DDD tactical building blocks mapped to
real classes, its invariants, the state machine, the CQRS split, the transactional outbox and
async consumers, the hexagonal ports/adapters, the persistence model, the error model, a live
API request/response, the test pyramid, the trade-offs, and future work.

The video is generated from repo-owned source — an animated web app in
`scripts/walkthrough/web` captured frame-by-frame through headless Chromium and encoded with
ffmpeg. Regenerate it (requires `node`, `npm`, and `ffmpeg`) with:

```bash
./scripts/generate-walkthrough-video.sh
```
