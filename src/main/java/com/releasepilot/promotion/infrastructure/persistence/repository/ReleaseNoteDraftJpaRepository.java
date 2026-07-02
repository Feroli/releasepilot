package com.releasepilot.promotion.infrastructure.persistence.repository;

import com.releasepilot.promotion.infrastructure.persistence.entity.ReleaseNoteDraftEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseNoteDraftJpaRepository extends JpaRepository<ReleaseNoteDraftEntity, UUID> {
    List<ReleaseNoteDraftEntity> findByPromotionIdOrderByCreatedAtDesc(UUID promotionId);
}

