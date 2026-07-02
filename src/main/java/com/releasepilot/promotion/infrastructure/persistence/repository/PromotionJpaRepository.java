package com.releasepilot.promotion.infrastructure.persistence;

import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.PromotionStatus;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionJpaRepository extends JpaRepository<PromotionEntity, UUID> {
    boolean existsByApplicationIdAndTargetEnvironmentAndStatusIn(
            String applicationId,
            Environment targetEnvironment,
            Collection<PromotionStatus> statuses);

    Page<PromotionEntity> findByApplicationId(String applicationId, Pageable pageable);
}

