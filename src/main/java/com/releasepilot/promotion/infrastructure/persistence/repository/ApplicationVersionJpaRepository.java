package com.releasepilot.promotion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationVersionJpaRepository extends JpaRepository<ApplicationVersionEntity, UUID> {
    Optional<ApplicationVersionEntity> findByApplicationIdAndVersion(String applicationId, String version);

    List<ApplicationVersionEntity> findByApplicationIdOrderByCreatedAtDesc(String applicationId);
}

