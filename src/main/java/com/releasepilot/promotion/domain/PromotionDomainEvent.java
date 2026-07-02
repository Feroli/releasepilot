package com.releasepilot.promotion.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PromotionDomainEvent(
        UUID eventId,
        String eventType,
        UUID promotionId,
        String applicationId,
        UUID versionId,
        String version,
        Environment sourceEnvironment,
        Environment targetEnvironment,
        String actingUser,
        Instant occurredAt,
        Map<String, String> payload
) {
    public static final String REQUESTED = "PromotionRequested";
    public static final String APPROVED = "PromotionApproved";
    public static final String DEPLOYMENT_STARTED = "DeploymentStarted";
    public static final String COMPLETED = "PromotionCompleted";
    public static final String ROLLED_BACK = "PromotionRolledBack";
    public static final String CANCELLED = "PromotionCancelled";
}

