package com.releasepilot.promotion.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "applications")
public class ApplicationEntity {
    @Id
    private String id;
    private String name;
    private Instant createdAt;

    protected ApplicationEntity() {
    }

    public ApplicationEntity(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

