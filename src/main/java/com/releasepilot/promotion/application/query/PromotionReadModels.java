package com.releasepilot.promotion.application.query;

import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.PromotionStatus;
import com.releasepilot.promotion.infrastructure.persistence.entity.EnvironmentState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PromotionReadModels {
    private PromotionReadModels() {
    }

    public record PromotionResponse(
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
            Instant terminalAt
    ) {
    }

    public record PromotionDetailResponse(
            PromotionResponse promotion,
            List<PromotionHistoryItem> history
    ) {
    }

    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record PromotionHistoryItem(
            UUID eventId,
            String eventType,
            String actingUser,
            Instant occurredAt
    ) {
    }

    public record ApplicationEnvironmentStatusResponse(
            String applicationId,
            List<EnvironmentStatusItem> environments
    ) {
    }

    public record EnvironmentStatusItem(
            Environment environment,
            UUID versionId,
            String version,
            EnvironmentState state,
            Instant completedAt,
            Instant updatedAt
    ) {
    }

    public record AuditLogResponse(
            UUID eventId,
            UUID promotionId,
            String eventType,
            String actingUser,
            Instant occurredAt,
            Instant consumedAt
    ) {
    }

    public record ReleaseNoteDraftResponse(
            UUID id,
            UUID promotionId,
            String draft,
            String status,
            Instant createdAt
    ) {
    }
}

