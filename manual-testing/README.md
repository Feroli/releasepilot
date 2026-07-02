# Manual Testing

This folder contains a Postman setup for running the complete ReleasePilot workflow against a local API.

## Setup

Start the service:

```bash
docker compose up -d
./gradlew bootRun
```

Import these files into Postman:

- `postman/ReleasePilot.local.postman_environment.json`
- `postman/ReleasePilot.workflow.postman_collection.json`

Select the `ReleasePilot Local` environment and run the collection in order.

## Workflow Coverage

The collection covers:

- Application setup.
- Version registration with `DEV` completed.
- `DEV -> STAGING` promotion request.
- Approval.
- Deployment start.
- Completion.
- Promotion detail read model.
- Application status read model.
- Promotion history read model.
- Async audit log inspection.
- Release notes inspection.
- Environment skip rejection.
- Non-approver rejection.
- Duplicate active promotion rejection.

