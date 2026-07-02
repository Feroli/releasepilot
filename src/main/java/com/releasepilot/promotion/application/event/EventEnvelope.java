package com.releasepilot.promotion.application.event;

import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.PromotionDomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventEnvelope(
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
    public static EventEnvelope from(PromotionDomainEvent event) {
        return new EventEnvelope(
                event.eventId(),
                event.eventType(),
                event.promotionId(),
                event.applicationId(),
                event.versionId(),
                event.version(),
                event.sourceEnvironment(),
                event.targetEnvironment(),
                event.actingUser(),
                event.occurredAt(),
                event.payload());
    }
}

