package com.releasepilot.web;

import com.releasepilot.promotion.application.query.PromotionQueryService;
import com.releasepilot.promotion.application.query.PromotionReadModels.ApplicationEnvironmentStatusResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.AuditLogResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionDetailResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionReadModels.ReleaseNoteDraftResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
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
    Page<PromotionResponse> getApplicationPromotions(
            @PathVariable String applicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queryService.getPromotionHistory(
                applicationId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
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

