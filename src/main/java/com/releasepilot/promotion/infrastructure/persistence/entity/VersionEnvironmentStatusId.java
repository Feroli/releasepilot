package com.releasepilot.promotion.infrastructure.persistence.entity;

import com.releasepilot.promotion.domain.Environment;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class VersionEnvironmentStatusId implements Serializable {
    private String applicationId;
    private UUID versionId;
    private Environment environment;

    public VersionEnvironmentStatusId() {
    }

    public VersionEnvironmentStatusId(String applicationId, UUID versionId, Environment environment) {
        this.applicationId = applicationId;
        this.versionId = versionId;
        this.environment = environment;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VersionEnvironmentStatusId that)) {
            return false;
        }
        return Objects.equals(applicationId, that.applicationId)
                && Objects.equals(versionId, that.versionId)
                && environment == that.environment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, versionId, environment);
    }
}

