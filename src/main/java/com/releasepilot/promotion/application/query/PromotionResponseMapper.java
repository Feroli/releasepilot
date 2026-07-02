package com.releasepilot.promotion.application.query;

import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEntity;

public final class PromotionResponseMapper {
    private PromotionResponseMapper() {
    }

    public static PromotionResponse from(PromotionEntity entity) {
        return new PromotionResponse(
                entity.getId(),
                entity.getApplicationId(),
                entity.getVersionId(),
                entity.getVersion(),
                entity.getSourceEnvironment(),
                entity.getTargetEnvironment(),
                entity.getStatus(),
                entity.getRequestedBy(),
                entity.getApprovedBy(),
                entity.getDeploymentRef(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getTerminalAt());
    }
}

