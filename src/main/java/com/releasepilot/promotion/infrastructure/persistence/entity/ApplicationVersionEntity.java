package com.releasepilot.promotion.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_versions")
public class ApplicationVersionEntity {
    @Id
    private UUID id;
    private String applicationId;
    private String version;
    private Instant createdAt;

    protected ApplicationVersionEntity() {
    }

    public ApplicationVersionEntity(UUID id, String applicationId, String version, Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.version = version;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

