package com.releasepilot.promotion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {
    boolean existsByEventId(UUID eventId);

    List<AuditLogEntity> findByPromotionIdOrderByOccurredAtAsc(UUID promotionId);
}

