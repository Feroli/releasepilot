package com.releasepilot.promotion.infrastructure.persistence;

import com.releasepilot.promotion.domain.Environment;
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

