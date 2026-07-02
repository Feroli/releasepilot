CREATE TABLE applications (
    id VARCHAR(120) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE application_versions (
    id UUID PRIMARY KEY,
    application_id VARCHAR(120) NOT NULL REFERENCES applications(id),
    version VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_application_versions_application_version UNIQUE (application_id, version)
);

CREATE TABLE version_environment_status (
    application_id VARCHAR(120) NOT NULL REFERENCES applications(id),
    version_id UUID NOT NULL REFERENCES application_versions(id),
    environment VARCHAR(40) NOT NULL,
    version VARCHAR(120) NOT NULL,
    state VARCHAR(40) NOT NULL,
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (application_id, version_id, environment)
);

CREATE TABLE promotions (
    id UUID PRIMARY KEY,
    application_id VARCHAR(120) NOT NULL REFERENCES applications(id),
    version_id UUID NOT NULL REFERENCES application_versions(id),
    version VARCHAR(120) NOT NULL,
    source_environment VARCHAR(40) NOT NULL,
    target_environment VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    requested_by VARCHAR(120) NOT NULL,
    approved_by VARCHAR(120),
    deployment_ref VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    terminal_at TIMESTAMPTZ,
    aggregate_version INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX ux_active_promotion_target
ON promotions(application_id, target_environment)
WHERE status IN ('REQUESTED', 'APPROVED', 'DEPLOYING');

CREATE TABLE promotion_events (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    event_type VARCHAR(120) NOT NULL,
    acting_user VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(120) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    promotion_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    acting_user VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE release_note_drafts (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    draft TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
