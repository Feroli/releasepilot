package com.releasepilot.promotion.application.query;

import com.releasepilot.promotion.application.query.PromotionReadModels.ApplicationEnvironmentStatusResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.AuditLogResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.EnvironmentStatusItem;
import com.releasepilot.promotion.application.query.PromotionReadModels.PagedResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionDetailResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionHistoryItem;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.ReleaseNoteDraftResponse;
import com.releasepilot.promotion.domain.DomainErrorCode;
import com.releasepilot.promotion.domain.DomainException;
import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.infrastructure.persistence.repository.ApplicationJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.repository.AuditLogJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.repository.PromotionEventJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.repository.PromotionJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.repository.ReleaseNoteDraftJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.entity.VersionEnvironmentStatusEntity;
import com.releasepilot.promotion.infrastructure.persistence.repository.VersionEnvironmentStatusJpaRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionQueryService {
    private final PromotionJpaRepository promotionRepository;
    private final PromotionEventJpaRepository eventRepository;
    private final VersionEnvironmentStatusJpaRepository statusRepository;
    private final AuditLogJpaRepository auditLogRepository;
    private final ReleaseNoteDraftJpaRepository draftRepository;
    private final ApplicationJpaRepository applicationRepository;

    public PromotionQueryService(
            PromotionJpaRepository promotionRepository,
            PromotionEventJpaRepository eventRepository,
            VersionEnvironmentStatusJpaRepository statusRepository,
            AuditLogJpaRepository auditLogRepository,
            ReleaseNoteDraftJpaRepository draftRepository,
            ApplicationJpaRepository applicationRepository) {
        this.promotionRepository = promotionRepository;
        this.eventRepository = eventRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.draftRepository = draftRepository;
        this.applicationRepository = applicationRepository;
    }

    private void requireApplication(String applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Application not found");
        }
    }

    @Transactional(readOnly = true)
    public PromotionDetailResponse getPromotion(UUID promotionId) {
        PromotionResponse promotion = promotionRepository.findById(promotionId)
                .map(PromotionResponseMapper::from)
                .orElseThrow(() -> new DomainException(DomainErrorCode.RESOURCE_NOT_FOUND, "Promotion not found"));
        List<PromotionHistoryItem> history = eventRepository.findByPromotionIdOrderByOccurredAtAsc(promotionId)
                .stream()
                .map(event -> new PromotionHistoryItem(
                        event.getId(),
                        event.getEventType(),
                        event.getActingUser(),
                        event.getOccurredAt()))
                .toList();
        return new PromotionDetailResponse(promotion, history);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PromotionResponse> getPromotionHistory(String applicationId, Pageable pageable) {
        requireApplication(applicationId);
        Page<PromotionResponse> page = promotionRepository.findByApplicationId(applicationId, pageable)
                .map(PromotionResponseMapper::from);
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ApplicationEnvironmentStatusResponse getApplicationStatus(String applicationId) {
        requireApplication(applicationId);
        Map<Environment, VersionEnvironmentStatusEntity> latestByEnvironment =
                statusRepository.findByApplicationId(applicationId).stream()
                        .collect(Collectors.groupingBy(
                                VersionEnvironmentStatusEntity::getEnvironment,
                                Collectors.collectingAndThen(
                                        Collectors.maxBy(Comparator.comparing(VersionEnvironmentStatusEntity::getUpdatedAt)),
                                        optional -> optional.orElse(null))));
        List<EnvironmentStatusItem> environments = Arrays.stream(Environment.values())
                .map(environment -> {
                    VersionEnvironmentStatusEntity status = latestByEnvironment.get(environment);
                    if (status == null) {
                        return new EnvironmentStatusItem(environment, null, null, null, null, null);
                    }
                    return new EnvironmentStatusItem(
                            environment,
                            status.getVersionId(),
                            status.getVersion(),
                            status.getState(),
                            status.getCompletedAt(),
                            status.getUpdatedAt());
                })
                .toList();
        return new ApplicationEnvironmentStatusResponse(applicationId, environments);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLog(UUID promotionId) {
        return auditLogRepository.findByPromotionIdOrderByOccurredAtAsc(promotionId).stream()
                .map(audit -> new AuditLogResponse(
                        audit.getEventId(),
                        audit.getPromotionId(),
                        audit.getEventType(),
                        audit.getActingUser(),
                        audit.getOccurredAt(),
                        audit.getConsumedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReleaseNoteDraftResponse> getReleaseNoteDrafts(UUID promotionId) {
        return draftRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId).stream()
                .map(draft -> new ReleaseNoteDraftResponse(
                        draft.getId(),
                        draft.getPromotionId(),
                        draft.getDraft(),
                        draft.getStatus(),
                        draft.getCreatedAt()))
                .toList();
    }
}

