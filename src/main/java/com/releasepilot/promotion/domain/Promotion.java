package com.releasepilot.promotion.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Promotion {
    private static final EnvironmentPipeline PIPELINE = new EnvironmentPipeline();

    private final UUID id;
    private final String applicationId;
    private final UUID versionId;
    private final String version;
    private final Environment sourceEnvironment;
    private final Environment targetEnvironment;
    private PromotionStatus status;
    private final String requestedBy;
    private String approvedBy;
    private String deploymentRef;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant terminalAt;
    private final List<PromotionDomainEvent> recordedEvents;

    private Promotion(
            UUID id,
            String applicationId,
            UUID versionId,
            String version,
            Environment sourceEnvironment,
            Environment targetEnvironment,
            PromotionStatus status,
            String requestedBy,
            String approvedBy,
            String deploymentRef,
            Instant createdAt,
            Instant updatedAt,
            Instant terminalAt,
            List<PromotionDomainEvent> recordedEvents) {
        this.id = id;
        this.applicationId = applicationId;
        this.versionId = versionId;
        this.version = version;
        this.sourceEnvironment = sourceEnvironment;
        this.targetEnvironment = targetEnvironment;
        this.status = status;
        this.requestedBy = requestedBy;
        this.approvedBy = approvedBy;
        this.deploymentRef = deploymentRef;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.terminalAt = terminalAt;
        this.recordedEvents = new ArrayList<>(recordedEvents);
    }

    public static Promotion request(
            UUID id,
            String applicationId,
            UUID versionId,
            String version,
            Environment sourceEnvironment,
            Environment targetEnvironment,
            String requestedBy,
            PromotionEligibility eligibility,
            Instant now) {
        PIPELINE.requireNextStep(sourceEnvironment, targetEnvironment);
        if (!eligibility.sourceEnvironmentCompleted()) {
            throw new DomainException(
                    DomainErrorCode.PREVIOUS_ENVIRONMENT_INCOMPLETE,
                    "Source environment must be completed before promotion");
        }
        if (eligibility.activePromotionForTargetExists()) {
            throw new DomainException(
                    DomainErrorCode.PROMOTION_ALREADY_IN_PROGRESS,
                    "A promotion is already in progress for this application and target environment");
        }
        Promotion promotion = new Promotion(
                id,
                applicationId,
                versionId,
                version,
                sourceEnvironment,
                targetEnvironment,
                PromotionStatus.REQUESTED,
                requestedBy,
                null,
                null,
                now,
                now,
                null,
                List.of());
        promotion.record(PromotionDomainEvent.REQUESTED, requestedBy, now, Map.of());
        return promotion;
    }

    public static Promotion rehydrate(
            UUID id,
            String applicationId,
            UUID versionId,
            String version,
            Environment sourceEnvironment,
            Environment targetEnvironment,
            PromotionStatus status,
            String requestedBy,
            String approvedBy,
            String deploymentRef,
            Instant createdAt,
            Instant updatedAt,
            Instant terminalAt) {
        return new Promotion(
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
                terminalAt,
                List.of());
    }

    public void approve(String actingUser, boolean approver, Instant now) {
        requireMutable();
        requireStatus(PromotionStatus.REQUESTED);
        if (!approver) {
            throw new DomainException(DomainErrorCode.APPROVER_REQUIRED, "Only approvers may approve promotions");
        }
        approvedBy = actingUser;
        status = PromotionStatus.APPROVED;
        updatedAt = now;
        record(PromotionDomainEvent.APPROVED, actingUser, now, Map.of());
    }

    public void startDeployment(String actingUser, String deploymentRef, Instant now) {
        requireMutable();
        requireStatus(PromotionStatus.APPROVED);
        this.deploymentRef = deploymentRef;
        status = PromotionStatus.DEPLOYING;
        updatedAt = now;
        record(PromotionDomainEvent.DEPLOYMENT_STARTED, actingUser, now, Map.of("deploymentRef", deploymentRef));
    }

    public void complete(String actingUser, Instant now) {
        requireMutable();
        requireStatus(PromotionStatus.DEPLOYING);
        status = PromotionStatus.COMPLETED;
        updatedAt = now;
        terminalAt = now;
        record(PromotionDomainEvent.COMPLETED, actingUser, now, Map.of());
    }

    public void rollBack(String actingUser, String reason, Instant now) {
        requireMutable();
        if (status != PromotionStatus.APPROVED && status != PromotionStatus.DEPLOYING) {
            throw new DomainException(
                    DomainErrorCode.INVALID_PROMOTION_STATE,
                    "Promotion can only be rolled back after approval and before completion");
        }
        status = PromotionStatus.ROLLED_BACK;
        updatedAt = now;
        terminalAt = now;
        record(PromotionDomainEvent.ROLLED_BACK, actingUser, now, Map.of("reason", reason));
    }

    public void cancel(String actingUser, String reason, Instant now) {
        requireMutable();
        status = PromotionStatus.CANCELLED;
        updatedAt = now;
        terminalAt = now;
        record(PromotionDomainEvent.CANCELLED, actingUser, now, Map.of("reason", reason));
    }

    public List<PromotionDomainEvent> pullRecordedEvents() {
        List<PromotionDomainEvent> events = List.copyOf(recordedEvents);
        recordedEvents.clear();
        return events;
    }

    private void requireMutable() {
        if (status.isTerminal()) {
            throw new DomainException(DomainErrorCode.PROMOTION_IMMUTABLE, "Terminal promotions cannot be changed");
        }
    }

    private void requireStatus(PromotionStatus expected) {
        if (status != expected) {
            throw new DomainException(
                    DomainErrorCode.INVALID_PROMOTION_STATE,
                    "Expected status " + expected + " but was " + status);
        }
    }

    private void record(String eventType, String actingUser, Instant now, Map<String, String> payload) {
        recordedEvents.add(new PromotionDomainEvent(
                UUID.randomUUID(),
                eventType,
                id,
                applicationId,
                versionId,
                version,
                sourceEnvironment,
                targetEnvironment,
                actingUser,
                now,
                payload));
    }

    public UUID id() {
        return id;
    }

    public String applicationId() {
        return applicationId;
    }

    public UUID versionId() {
        return versionId;
    }

    public String version() {
        return version;
    }

    public Environment sourceEnvironment() {
        return sourceEnvironment;
    }

    public Environment targetEnvironment() {
        return targetEnvironment;
    }

    public PromotionStatus status() {
        return status;
    }

    public String requestedBy() {
        return requestedBy;
    }

    public String approvedBy() {
        return approvedBy;
    }

    public String deploymentRef() {
        return deploymentRef;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant terminalAt() {
        return terminalAt;
    }
}

