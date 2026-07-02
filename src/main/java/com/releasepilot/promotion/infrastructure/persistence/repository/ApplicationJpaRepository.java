package com.releasepilot.promotion.infrastructure.persistence.repository;

import com.releasepilot.promotion.infrastructure.persistence.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, String> {
}

