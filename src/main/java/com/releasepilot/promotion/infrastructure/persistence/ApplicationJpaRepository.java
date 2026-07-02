package com.releasepilot.promotion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, String> {
}

