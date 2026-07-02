package com.releasepilot.promotion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionEventJpaRepository extends JpaRepository<PromotionEventEntity, UUID> {
    List<PromotionEventEntity> findByPromotionIdOrderByOccurredAtAsc(UUID promotionId);
}

