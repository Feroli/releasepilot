package com.releasepilot.promotion.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotion_events")
public class PromotionEventEntity {
    @Id
    private UUID id;
    private UUID promotionId;
    private String eventType;
    private String actingUser;
    @Lob
    private String payload;
    private Instant occurredAt;

    protected PromotionEventEntity() {
    }

    public PromotionEventEntity(UUID id, UUID promotionId, String eventType, String actingUser, String payload, Instant occurredAt) {
        this.id = id;
        this.promotionId = promotionId;
        this.eventType = eventType;
        this.actingUser = actingUser;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
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

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}

