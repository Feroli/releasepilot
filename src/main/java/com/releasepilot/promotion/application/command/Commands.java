package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.domain.Environment;
import java.util.UUID;

public final class Commands {
    private Commands() {
    }

    public record RequestPromotionCommand(
            String applicationId,
            String version,
            Environment sourceEnvironment,
            Environment targetEnvironment,
            String requestedBy
    ) {
    }

    public record ApprovePromotionCommand(UUID promotionId, String actingUser) {
    }

    public record StartDeploymentCommand(UUID promotionId, String actingUser) {
    }

    public record CompletePromotionCommand(UUID promotionId, String actingUser) {
    }

    public record RollbackPromotionCommand(UUID promotionId, String actingUser, String reason) {
    }

    public record CancelPromotionCommand(UUID promotionId, String actingUser, String reason) {
    }
}

