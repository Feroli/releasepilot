package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.event.DomainEventRecorder;
import com.releasepilot.promotion.domain.DomainErrorCode;
import com.releasepilot.promotion.domain.DomainException;
import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.domain.PromotionEligibility;
import com.releasepilot.promotion.domain.PromotionStatus;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationVersionEntity;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationVersionJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEntity;
import com.releasepilot.promotion.infrastructure.persistence.PromotionJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusEntity;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusId;
import com.releasepilot.promotion.infrastructure.persistence.VersionEnvironmentStatusJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class PromotionCommandSupport {
    private static final List<PromotionStatus> ACTIVE_STATUSES = List.of(
            PromotionStatus.REQUESTED,
            PromotionStatus.APPROVED,
            PromotionStatus.DEPLOYING);

    private final PromotionJpaRepository promotionRepository;
    private final ApplicationVersionJpaRepository versionRepository;
    private final VersionEnvironmentStatusJpaRepository statusRepository;
    private final DomainEventRecorder eventRecorder;
    private final Clock clock;

    public PromotionCommandSupport(
            PromotionJpaRepository promotionRepository,
            ApplicationVersionJpaRepository versionRepository,
            VersionEnvironmentStatusJpaRepository statusRepository,
            DomainEventRecorder eventRecorder,
            Clock clock) {
        this.promotionRepository = promotionRepository;
        this.versionRepository = versionRepository;
        this.statusRepository = statusRepository;
        this.eventRecorder = eventRecorder;
        this.clock = clock;
    }

    public ApplicationVersionEntity requireVersion(String applicationId, String version) {
        return versionRepository.findByApplicationIdAndVersion(applicationId, version)
                .orElseThrow(() -> new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Version not found"));
    }

    public Promotion requirePromotion(UUID promotionId) {
        return promotionRepository.findById(promotionId)
                .map(PromotionEntity::toDomain)
                .orElseThrow(() -> new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Promotion not found"));
    }

    public PromotionEligibility eligibility(String applicationId, UUID versionId, Environment source, Environment target) {
        boolean sourceCompleted = statusRepository.existsByApplicationIdAndVersionIdAndEnvironmentAndState(
                applicationId,
                versionId,
                source,
                EnvironmentState.COMPLETED);
        boolean activeExists = promotionRepository.existsByApplicationIdAndTargetEnvironmentAndStatusIn(
                applicationId,
                target,
                ACTIVE_STATUSES);
        return new PromotionEligibility(sourceCompleted, activeExists);
    }

    public Promotion saveAndRecord(Promotion promotion) {
        try {
            PromotionEntity saved = promotionRepository.save(PromotionEntity.from(promotion));
            eventRecorder.record(promotion.pullRecordedEvents());
            return saved.toDomain();
        } catch (DataIntegrityViolationException ex) {
            throw new DomainException(
                    DomainErrorCode.PROMOTION_ALREADY_IN_PROGRESS,
                    "A promotion is already in progress for this application and target environment");
        }
    }

    public void updateTargetStatus(Promotion promotion, EnvironmentState state) {
        Instant now = Instant.now(clock);
        VersionEnvironmentStatusId id = new VersionEnvironmentStatusId(
                promotion.applicationId(),
                promotion.versionId(),
                promotion.targetEnvironment());
        VersionEnvironmentStatusEntity status = statusRepository.findById(id)
                .orElse(new VersionEnvironmentStatusEntity(
                        promotion.applicationId(),
                        promotion.versionId(),
                        promotion.version(),
                        promotion.targetEnvironment(),
                        state,
                        state == EnvironmentState.COMPLETED ? now : null,
                        now));
        status.update(state, state == EnvironmentState.COMPLETED ? now : status.getCompletedAt(), now);
        statusRepository.save(status);
    }

    public Instant now() {
        return Instant.now(clock);
    }
}

