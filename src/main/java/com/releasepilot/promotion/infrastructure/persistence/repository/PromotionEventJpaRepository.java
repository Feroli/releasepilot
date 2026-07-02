package com.releasepilot.promotion.infrastructure.persistence.repository;

import com.releasepilot.promotion.infrastructure.persistence.entity.PromotionEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionEventJpaRepository extends JpaRepository<PromotionEventEntity, UUID> {
    List<PromotionEventEntity> findByPromotionIdOrderByOccurredAtAsc(UUID promotionId);
}

