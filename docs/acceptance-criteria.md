# ReleasePilot Acceptance Criteria

Source: The Agile Monkeys ReleasePilot backend challenge page, fetched on 2026-07-02.

## Challenge Goal

Build ReleasePilot, a backend REST API that manages how application versions move through deployment environments. The core of the system is a Promotion domain model that advances one application version through an ordered deployment pipeline: `dev -> staging -> production`.

The evaluator is explicitly looking for domain modeling, DDD, CQRS, domain events, ports/adapters, async processing, and clear ownership of trade-offs.

## P0 Acceptance Criteria

### 1. Runnable Backend Service

- The project is implemented as a Java backend REST API.
- The API can be started locally using documented commands.
- The repository includes `docker-compose.yml`.
- `docker-compose.yml` starts every required infrastructure dependency, at minimum:
  - PostgreSQL database.
  - Message queue for domain events, recommended RabbitMQ.
- The application can connect to the composed infrastructure without manual setup.
- The README documents prerequisites, infrastructure startup, API startup, and verification commands.

### 2. Deployment Pipeline Model

- The system models an ordered environment pipeline: `dev -> staging -> production`.
- The ordering is enforced by domain logic, not only by controller validation.
- A promotion can advance a version by exactly one environment step.
- A version cannot skip an environment.
- A version cannot move to an environment unless it has completed the previous environment.
- Invalid environment transitions produce domain errors and never unexpected HTTP 500 responses.

Suggested API behavior:

- Requesting `dev -> staging` is valid when the version has completed `dev`.
- Requesting `staging -> production` is valid when the version has completed `staging`.
- Requesting `dev -> production` is rejected with an environment-skipped domain error.
- Requesting `staging -> production` before staging completion is rejected.

### 3. Promotion Aggregate

- `Promotion` is modeled as a domain aggregate.
- The aggregate guards its own lifecycle/state transition rules.
- The aggregate is not a passive persistence record.
- Controllers do not contain business decision logic.
- Invalid command attempts throw typed domain errors.
- Completed, cancelled, and rolled-back promotions are immutable.
- The aggregate records domain events during state transitions.

Required aggregate data:

- Promotion id.
- Application id.
- Application version id or version label.
- Source environment.
- Target environment.
- Status.
- Requested by.
- Approved by, when applicable.
- Deployment reference, when applicable.
- Created, updated, and terminal timestamps.
- Domain event history or event references for query history.

### 4. Promotion Uniqueness

- Only one promotion may be in progress for the same application and target environment.
- "In progress" includes requested, approved, and deploying states.
- Terminal states do not block later promotions.
- The rule is enforced in application/domain logic.
- The rule is backed by persistence constraints or transactional locking so concurrent requests cannot create duplicates.
- Violations return a domain/client error rather than HTTP 500.

### 5. Approval Rule

- Only an approver may approve a promotion.
- The approver check is explicit and testable.
- Approver data may be stubbed or in memory for the challenge.
- Non-approvers receive a domain/client error.
- Approving a terminal promotion is rejected.
- Approving a promotion in an invalid state is rejected.

### 6. Command Side

Each command has a dedicated handler. Command handlers are thin orchestration components around the aggregate, repositories, ports, and event publishing.

Required commands and emitted events:

| Command | Event |
| --- | --- |
| `RequestPromotion` | `PromotionRequested` |
| `ApprovePromotion` | `PromotionApproved` |
| `StartDeployment` | `DeploymentStarted` |
| `CompletePromotion` | `PromotionCompleted` |
| `RollbackPromotion` | `PromotionRolledBack` |
| `CancelPromotion` | `PromotionCancelled` |

Acceptance criteria:

- Every command has one handler class.
- Every successful state transition emits exactly the expected domain event.
- Failed commands do not emit domain events.
- Command handlers persist aggregate changes transactionally.
- Command handlers return stable response DTOs.
- Command handlers are covered by tests for success, invalid state, and authorization/domain failures.

### 7. Query Side

The query side returns read models shaped for API consumers, not aggregate entities.

Required read endpoints:

| Endpoint | Expected read model |
| --- | --- |
| `GET /promotions/{id}` | Promotion detail plus state history |
| `GET /applications/{id}/status` | Current state per environment |
| `GET /applications/{id}/promotions` | Paged promotion history |

Acceptance criteria:

- Query handlers do not expose JPA entities directly.
- Query handlers are separate from command handlers.
- Promotion detail includes current status and chronological history.
- Application status shows each environment and the version currently completed or deploying there.
- Promotion history supports pagination.
- Not-found cases return HTTP 404 with a structured error body.

### 8. External System Ports

The platform defines interfaces for external systems and supplies in-memory or stubbed adapters.

Required ports:

| Port | Responsibility |
| --- | --- |
| `DeploymentPort` | Trigger a deployment |
| `IssueTrackerPort` | Fetch linked work items |
| `NotificationPort` | Send notifications on terminal states |

Acceptance criteria:

- Ports are interfaces owned by the core/application boundary, not by infrastructure adapters.
- Adapters live in the infrastructure layer.
- No real HTTP integration is required.
- Stub adapters are deterministic and testable.
- The design explains why the interfaces are placed where they are.

### 9. Domain Events and Async Processing

- Every domain event is published to a queue.
- Event consumers are decoupled from the HTTP request lifecycle.
- The API responds before async consumers finish.
- The consumer design could run as a separate process.
- Event publishing is reliable enough for the challenge; recommended implementation is transactional outbox plus queue publisher.

Required audit consumer:

- Consumes every domain event.
- Persists an audit log row for each event.
- Audit rows include:
  - Event type.
  - Promotion id.
  - Timestamp.
  - Acting user.
- Audit consumer is idempotent.
- Audit persistence failures do not change the result of the already-completed API command.

### 10. Error Handling

- Domain validation failures return structured client errors.
- Unexpected exceptions return HTTP 500 with a generic message.
- Invalid request payloads return HTTP 400.
- Missing resources return HTTP 404.
- Duplicate in-progress promotions return HTTP 409.
- Invalid state transitions return HTTP 409.
- Non-approver approval attempts return HTTP 403.
- Error bodies use one consistent shape, preferably RFC 7807 Problem Details.

### 11. README and Delivery

The repository includes a README with:

- Prerequisites.
- How to start infrastructure.
- How to run the API.
- How to run tests.
- How to stop/reset the environment.
- At least one example request for each command:
  - Request promotion.
  - Approve promotion.
  - Start deployment.
  - Complete promotion.
  - Roll back promotion.
  - Cancel promotion.
- Example query requests.
- Brief explanation of DDD, CQRS, events, and ports/adapters in the implementation.
- Known trade-offs and unfinished work.

### 12. AI Session Record

- The repository includes the record of how AI was used.
- Acceptable forms:
  - `AI_SESSION_LOG.md`.
  - Exported chat transcript.
  - Prompt log.
  - Screenshots.
- The record should make it clear which decisions were accepted by the developer and why.

### 13. Walkthrough Artifact

The final delivery includes an async walkthrough in a compact format:

- Under 10 minutes if video.
- Under 15 slides if deck.
- Covers:
  - Domain model.
  - Where invariants live.
  - CQRS design.
  - Event publishing and consumers.
  - Port/adapters placement.
  - Trade-offs.
  - What would be improved next.

## P1 Acceptance Criteria

### 1. Supporting Application and Version Management

The challenge does not explicitly require CRUD for applications and versions, but a complete API benefits from minimal support endpoints.

Suggested criteria:

- Create an application.
- Register a version for an application.
- Mark or seed the initial `dev` completion required for the first promotion.
- Retrieve applications and versions for testing/demo.
- Keep this support surface small so it does not distract from the Promotion engine.

### 2. Concurrency and Idempotency

- Concurrent duplicate promotion requests cannot bypass the one-in-progress rule.
- Command requests can include an idempotency key.
- Repeated command requests with the same key return the original result where practical.
- Event consumers deduplicate by event id.

### 3. Observability

- Logs include command name, promotion id, application id, acting user, and event id where available.
- Health endpoints report database and queue connectivity.
- Basic metrics are exposed through Spring Boot Actuator.

## Stretch Acceptance Criteria

### AI Release Notes Agent

When a promotion reaches Approved:

- The system triggers a release-notes agent.
- The agent is not a single prompt wrapper.
- The agent follows a tool-calling loop.
- A mocked LLM backend is acceptable.
- Release notes drafts are persisted.
- Agent failures are observable and do not block the approval command response.

Required agent tools:

| Tool | Responsibility |
| --- | --- |
| `GetWorkItems(promotionId)` | Fetch linked issue stubs |
| `AskClarification(workItemId, question)` | Record clarification requests |
| `FlagBreakingChange(workItemId, reason)` | Mark possible breaking changes |
| `SubmitReleaseNotes(draft)` | Persist the release-notes draft |

Suggested criteria:

- Agent starts from `PromotionApproved`.
- Agent fetches linked work items through `IssueTrackerPort`.
- Agent can ask for clarification when issue data is incomplete.
- Agent can flag breaking changes based on issue metadata.
- Agent submits a draft with summary, changes, risks, and breaking-change notes.
- The implementation is structurally clear even if the LLM is mocked.

## Suggested Definition of Done

- All P0 acceptance criteria are met.
- Tests cover the state machine and all domain invariants.
- At least one happy-path end-to-end flow is runnable from README commands.
- At least three negative-path examples are documented and tested:
  - Environment skip.
  - Duplicate in-progress promotion.
  - Non-approver approval.
- Queue-backed audit logging works asynchronously.
- The README and walkthrough explain trade-offs honestly.
- Optional stretch work is attempted only after the core engine is solid.
