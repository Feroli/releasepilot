package com.releasepilot.promotion.infrastructure.persistence.entity;

import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.domain.PromotionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotions")
public class PromotionEntity {
    @Id
    private UUID id;
    private String applicationId;
    private UUID versionId;
    private String version;
    @Enumerated(EnumType.STRING)
    private Environment sourceEnvironment;
    @Enumerated(EnumType.STRING)
    private Environment targetEnvironment;
    @Enumerated(EnumType.STRING)
    private PromotionStatus status;
    private String requestedBy;
    private String approvedBy;
    private String deploymentRef;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant terminalAt;
    private int aggregateVersion;

    protected PromotionEntity() {
    }

    public static PromotionEntity from(Promotion promotion) {
        PromotionEntity entity = new PromotionEntity();
        entity.id = promotion.id();
        entity.applicationId = promotion.applicationId();
        entity.versionId = promotion.versionId();
        entity.version = promotion.version();
        entity.sourceEnvironment = promotion.sourceEnvironment();
        entity.targetEnvironment = promotion.targetEnvironment();
        entity.status = promotion.status();
        entity.requestedBy = promotion.requestedBy();
        entity.approvedBy = promotion.approvedBy();
        entity.deploymentRef = promotion.deploymentRef();
        entity.createdAt = promotion.createdAt();
        entity.updatedAt = promotion.updatedAt();
        entity.terminalAt = promotion.terminalAt();
        return entity;
    }

    public Promotion toDomain() {
        return Promotion.rehydrate(
                id,
                applicationId,
                versionId,
                version,
                sourceEnvironment,
                targetEnvironment,
                status,
                requestedBy,
                approvedBy,
                deploymentRef,
                createdAt,
                updatedAt,
                terminalAt);
    }

    public UUID getId() {
        return id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public String getVersion() {
        return version;
    }

    public Environment getSourceEnvironment() {
        return sourceEnvironment;
    }

    public Environment getTargetEnvironment() {
        return targetEnvironment;
    }

    public PromotionStatus getStatus() {
        return status;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getDeploymentRef() {
        return deploymentRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getTerminalAt() {
        return terminalAt;
    }
}
