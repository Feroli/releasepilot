package com.releasepilot.catalog.application;

import com.releasepilot.promotion.domain.DomainErrorCode;
import com.releasepilot.promotion.domain.DomainException;
import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationEntity;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationVersionEntity;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationVersionJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusEntity;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusId;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final ApplicationJpaRepository applicationRepository;
    private final ApplicationVersionJpaRepository versionRepository;
    private final VersionEnvironmentStatusJpaRepository statusRepository;
    private final Clock clock;

    public CatalogService(
            ApplicationJpaRepository applicationRepository,
            ApplicationVersionJpaRepository versionRepository,
            VersionEnvironmentStatusJpaRepository statusRepository,
            Clock clock) {
        this.applicationRepository = applicationRepository;
        this.versionRepository = versionRepository;
        this.statusRepository = statusRepository;
        this.clock = clock;
    }

    @Transactional
    public ApplicationEntity createApplication(String id, String name) {
        if (applicationRepository.existsById(id)) {
            throw new DomainException(DomainErrorCode.RESOURCE_ALREADY_EXISTS, "Application already exists");
        }
        return applicationRepository.save(new ApplicationEntity(id, name, Instant.now(clock)));
    }

    @Transactional
    public ApplicationVersionEntity registerVersion(String applicationId, String version, boolean devCompleted) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Application not found");
        }
        versionRepository.findByApplicationIdAndVersion(applicationId, version)
                .ifPresent(existing -> {
                    throw new DomainException(DomainErrorCode.RESOURCE_ALREADY_EXISTS, "Version already exists");
                });
        Instant now = Instant.now(clock);
        ApplicationVersionEntity saved = versionRepository.save(new ApplicationVersionEntity(
                UUID.randomUUID(),
                applicationId,
                version,
                now));
        if (devCompleted) {
            statusRepository.save(new VersionEnvironmentStatusEntity(
                    applicationId,
                    saved.getId(),
                    version,
                    Environment.DEV,
                    EnvironmentState.COMPLETED,
                    now,
                    now));
        }
        return saved;
    }

    @Transactional
    public void markEnvironmentCompleted(String applicationId, String version, Environment environment) {
        ApplicationVersionEntity versionEntity = versionRepository.findByApplicationIdAndVersion(applicationId, version)
                .orElseThrow(() -> new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Version not found"));
        Instant now = Instant.now(clock);
        VersionEnvironmentStatusId id = new VersionEnvironmentStatusId(applicationId, versionEntity.getId(), environment);
        VersionEnvironmentStatusEntity status = statusRepository.findById(id)
                .orElse(new VersionEnvironmentStatusEntity(
                        applicationId,
                        versionEntity.getId(),
                        versionEntity.getVersion(),
                        environment,
                        EnvironmentState.COMPLETED,
                        now,
                        now));
        status.update(EnvironmentState.COMPLETED, now, now);
        statusRepository.save(status);
    }
}

