package com.releasepilot.promotion.infrastructure.persistence.repository;

import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.infrastructure.persistence.entity.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.entity.VersionEnvironmentStatusEntity;
import com.releasepilot.promotion.infrastructure.persistence.entity.VersionEnvironmentStatusId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionEnvironmentStatusJpaRepository
        extends JpaRepository<VersionEnvironmentStatusEntity, VersionEnvironmentStatusId> {
    boolean existsByApplicationIdAndVersionIdAndEnvironmentAndState(
            String applicationId,
            UUID versionId,
            Environment environment,
            EnvironmentState state);

    List<VersionEnvironmentStatusEntity> findByApplicationId(String applicationId);
}

