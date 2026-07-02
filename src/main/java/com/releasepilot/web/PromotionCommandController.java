package com.releasepilot.web;

import com.releasepilot.promotion.application.command.ApprovePromotionHandler;
import com.releasepilot.promotion.application.command.CancelPromotionHandler;
import com.releasepilot.promotion.application.command.Commands.ApprovePromotionCommand;
import com.releasepilot.promotion.application.command.Commands.CancelPromotionCommand;
import com.releasepilot.promotion.application.command.Commands.CompletePromotionCommand;
import com.releasepilot.promotion.application.command.Commands.RequestPromotionCommand;
import com.releasepilot.promotion.application.command.Commands.RollbackPromotionCommand;
import com.releasepilot.promotion.application.command.Commands.StartDeploymentCommand;
import com.releasepilot.promotion.application.command.CompletePromotionHandler;
import com.releasepilot.promotion.application.command.RequestPromotionHandler;
import com.releasepilot.promotion.application.command.RollbackPromotionHandler;
import com.releasepilot.promotion.application.command.StartDeploymentHandler;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.domain.Environment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotions")
public class PromotionCommandController {
    private final RequestPromotionHandler requestPromotionHandler;
    private final ApprovePromotionHandler approvePromotionHandler;
    private final StartDeploymentHandler startDeploymentHandler;
    private final CompletePromotionHandler completePromotionHandler;
    private final RollbackPromotionHandler rollbackPromotionHandler;
    private final CancelPromotionHandler cancelPromotionHandler;

    public PromotionCommandController(
            RequestPromotionHandler requestPromotionHandler,
            ApprovePromotionHandler approvePromotionHandler,
            StartDeploymentHandler startDeploymentHandler,
            CompletePromotionHandler completePromotionHandler,
            RollbackPromotionHandler rollbackPromotionHandler,
            CancelPromotionHandler cancelPromotionHandler) {
        this.requestPromotionHandler = requestPromotionHandler;
        this.approvePromotionHandler = approvePromotionHandler;
        this.startDeploymentHandler = startDeploymentHandler;
        this.completePromotionHandler = completePromotionHandler;
        this.rollbackPromotionHandler = rollbackPromotionHandler;
        this.cancelPromotionHandler = cancelPromotionHandler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PromotionResponse requestPromotion(@Valid @RequestBody RequestPromotionRequest request) {
        return requestPromotionHandler.handle(new RequestPromotionCommand(
                request.applicationId(),
                request.version(),
                request.sourceEnvironment(),
                request.targetEnvironment(),
                request.requestedBy()));
    }

    @PostMapping("/{promotionId}/approve")
    PromotionResponse approve(@PathVariable UUID promotionId, @Valid @RequestBody ActingUserRequest request) {
        return approvePromotionHandler.handle(new ApprovePromotionCommand(promotionId, request.actingUser()));
    }

    @PostMapping("/{promotionId}/deployments")
    PromotionResponse startDeployment(@PathVariable UUID promotionId, @Valid @RequestBody ActingUserRequest request) {
        return startDeploymentHandler.handle(new StartDeploymentCommand(promotionId, request.actingUser()));
    }

    @PostMapping("/{promotionId}/complete")
    PromotionResponse complete(@PathVariable UUID promotionId, @Valid @RequestBody ActingUserRequest request) {
        return completePromotionHandler.handle(new CompletePromotionCommand(promotionId, request.actingUser()));
    }

    @PostMapping("/{promotionId}/rollback")
    PromotionResponse rollBack(@PathVariable UUID promotionId, @Valid @RequestBody ReasonRequest request) {
        return rollbackPromotionHandler.handle(new RollbackPromotionCommand(
                promotionId,
                request.actingUser(),
                request.reason()));
    }

    @PostMapping("/{promotionId}/cancel")
    PromotionResponse cancel(@PathVariable UUID promotionId, @Valid @RequestBody ReasonRequest request) {
        return cancelPromotionHandler.handle(new CancelPromotionCommand(
                promotionId,
                request.actingUser(),
                request.reason()));
    }

    public record RequestPromotionRequest(
            @NotBlank String applicationId,
            @NotBlank String version,
            @NotNull Environment sourceEnvironment,
            @NotNull Environment targetEnvironment,
            @NotBlank String requestedBy
    ) {
    }

    public record ActingUserRequest(@NotBlank String actingUser) {
    }

    public record ReasonRequest(@NotBlank String actingUser, @NotBlank String reason) {
    }
}

