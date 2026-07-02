# ReleasePilot Walkthrough Motion Video Script

This script maps to the generated motion video in `deliverables/walkthrough/releasepilot-walkthrough.mp4`.

## Opening

ReleasePilot is a backend REST API for moving application versions through a strict deployment pipeline: `dev -> staging -> production`.

The core design decision is to make Promotion the aggregate. A Promotion represents one version moving exactly one step to one target environment.

## Domain Model

The aggregate owns lifecycle rules. It rejects invalid state transitions, records acting users, prevents terminal mutation, and emits domain events for every successful command.

Rules that need facts outside the aggregate are gathered into an eligibility snapshot before aggregate creation. That includes previous environment completion and whether another promotion is already active for the same application and target environment.

## CQRS

The write side uses command handlers. Each command has one handler that loads state, calls the aggregate, saves changes, and stores domain events in the outbox.

The read side uses query handlers and DTOs shaped for consumers: promotion detail with state history, application environment status, and paged promotion history.

## Events and Async Processing

Every domain event is persisted to the outbox as part of the command transaction. A publisher sends those events to RabbitMQ after commit. Consumers run asynchronously, so the API response does not wait for audit logging.

The audit consumer stores event type, promotion id, timestamp, and acting user. It is idempotent by event id.

## Ports and Adapters

External systems are modeled as ports: deployment, issue tracker, notification, and approver lookup. The challenge uses deterministic stubs, but the application core does not know that.

## Trade-Offs

The design uses CQRS without full event sourcing to stay focused and achievable. It uses an outbox instead of direct queue publishing to avoid losing events. It keeps the environment pipeline fixed because configurability is future work, not required challenge scope.

## Closing

The repository includes local runtime instructions, command examples, query examples, tests, architecture diagrams, and the AI working record. The next improvements would be stronger idempotency keys, richer projection handling, and replacing stubs with real integrations.
