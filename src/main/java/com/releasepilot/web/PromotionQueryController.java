package com.releasepilot.web;

import com.releasepilot.promotion.application.query.PromotionQueryService;
import com.releasepilot.promotion.application.query.PromotionReadModels.ApplicationEnvironmentStatusResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.AuditLogResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionDetailResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PagedResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.ReleaseNoteDraftResponse;
import com.releasepilot.promotion.domain.DomainErrorCode;
import com.releasepilot.promotion.domain.DomainException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PromotionQueryController {
    private static final int MAX_PAGE_SIZE = 100;

    private final PromotionQueryService queryService;

    public PromotionQueryController(PromotionQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/promotions/{promotionId}")
    PromotionDetailResponse getPromotion(@PathVariable UUID promotionId) {
        return queryService.getPromotion(promotionId);
    }

    @GetMapping("/applications/{applicationId}/status")
    ApplicationEnvironmentStatusResponse getApplicationStatus(@PathVariable String applicationId) {
        return queryService.getApplicationStatus(applicationId);
    }

    @GetMapping("/applications/{applicationId}/promotions")
    PagedResponse<PromotionResponse> getApplicationPromotions(
            @PathVariable String applicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new DomainException(DomainErrorCode.INVALID_PAGINATION, "Page index must not be negative");
        }
        if (size < 1) {
            throw new DomainException(DomainErrorCode.INVALID_PAGINATION, "Page size must be at least 1");
        }
        int cappedSize = Math.min(size, MAX_PAGE_SIZE);
        return queryService.getPromotionHistory(
                applicationId,
                PageRequest.of(page, cappedSize, Sort.by("createdAt").descending()));
    }

    @GetMapping("/promotions/{promotionId}/audit-log")
    List<AuditLogResponse> getAuditLog(@PathVariable UUID promotionId) {
        return queryService.getAuditLog(promotionId);
    }

    @GetMapping("/promotions/{promotionId}/release-notes")
    List<ReleaseNoteDraftResponse> getReleaseNotes(@PathVariable UUID promotionId) {
        return queryService.getReleaseNoteDrafts(promotionId);
    }
}

