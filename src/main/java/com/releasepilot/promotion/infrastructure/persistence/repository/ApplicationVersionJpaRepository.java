package com.releasepilot.promotion.infrastructure.persistence.repository;

import com.releasepilot.promotion.infrastructure.persistence.entity.ApplicationVersionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationVersionJpaRepository extends JpaRepository<ApplicationVersionEntity, UUID> {
    Optional<ApplicationVersionEntity> findByApplicationIdAndVersion(String applicationId, String version);

    List<ApplicationVersionEntity> findByApplicationIdOrderByCreatedAtDesc(String applicationId);
}

