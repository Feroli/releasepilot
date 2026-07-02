package com.releasepilot.promotion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseNoteDraftJpaRepository extends JpaRepository<ReleaseNoteDraftEntity, UUID> {
    List<ReleaseNoteDraftEntity> findByPromotionIdOrderByCreatedAtDesc(UUID promotionId);
}

