package com.releasepilot.promotion.domain;

public record PromotionEligibility(
        boolean sourceEnvironmentCompleted,
        boolean activePromotionForTargetExists
) {
}

