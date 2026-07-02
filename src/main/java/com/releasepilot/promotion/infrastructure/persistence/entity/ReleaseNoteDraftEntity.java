package com.releasepilot.promotion.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "release_note_drafts")
public class ReleaseNoteDraftEntity {
    @Id
    private UUID id;
    private UUID promotionId;
    private String draft;
    private String status;
    private Instant createdAt;

    protected ReleaseNoteDraftEntity() {
    }

    public ReleaseNoteDraftEntity(UUID id, UUID promotionId, String draft, String status, Instant createdAt) {
        this.id = id;
        this.promotionId = promotionId;
        this.draft = draft;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPromotionId() {
        return promotionId;
    }

    public String getDraft() {
        return draft;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

