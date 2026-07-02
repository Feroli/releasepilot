package com.releasepilot.promotion.infrastructure.persistence.entity;

import com.releasepilot.promotion.domain.Environment;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@IdClass(VersionEnvironmentStatusId.class)
@Table(name = "version_environment_status")
public class VersionEnvironmentStatusEntity {
    @Id
    private String applicationId;
    @Id
    private UUID versionId;
    @Id
    @Enumerated(EnumType.STRING)
    private Environment environment;
    private String version;
    @Enumerated(EnumType.STRING)
    private EnvironmentState state;
    private Instant completedAt;
    private Instant updatedAt;

    protected VersionEnvironmentStatusEntity() {
    }

    public VersionEnvironmentStatusEntity(
            String applicationId,
            UUID versionId,
            String version,
            Environment environment,
            EnvironmentState state,
            Instant completedAt,
            Instant updatedAt) {
        this.applicationId = applicationId;
        this.versionId = versionId;
        this.version = version;
        this.environment = environment;
        this.state = state;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public void update(EnvironmentState state, Instant completedAt, Instant updatedAt) {
        this.state = state;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getVersion() {
        return version;
    }

    public EnvironmentState getState() {
        return state;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

