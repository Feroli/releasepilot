package com.releasepilot.promotion.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
    @Id
    private UUID id;
    private UUID eventId;
    private UUID promotionId;
    private String eventType;
    private String actingUser;
    private Instant occurredAt;
    private Instant consumedAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(UUID id, UUID eventId, UUID promotionId, String eventType, String actingUser, Instant occurredAt, Instant consumedAt) {
        this.id = id;
        this.eventId = eventId;
        this.promotionId = promotionId;
        this.eventType = eventType;
        this.actingUser = actingUser;
        this.occurredAt = occurredAt;
        this.consumedAt = consumedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getPromotionId() {
        return promotionId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getActingUser() {
        return actingUser;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}

