package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.StartDeploymentCommand;
import com.releasepilot.promotion.application.port.DeploymentPort;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartDeploymentHandler {
    private final PromotionCommandSupport support;
    private final DeploymentPort deploymentPort;

    public StartDeploymentHandler(PromotionCommandSupport support, DeploymentPort deploymentPort) {
        this.support = support;
        this.deploymentPort = deploymentPort;
    }

    @Transactional
    public PromotionResponse handle(StartDeploymentCommand command) {
        Promotion promotion = support.requirePromotion(command.promotionId());
        String deploymentRef = deploymentPort.startDeployment(promotion, command.actingUser());
        promotion.startDeployment(command.actingUser(), deploymentRef, support.now());
        Promotion saved = support.saveAndRecord(promotion);
        support.updateTargetStatus(saved, EnvironmentState.DEPLOYING);
        return PromotionResponseMapper.from(PromotionEntity.from(saved));
    }
}

