package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.CancelPromotionCommand;
import com.releasepilot.promotion.application.port.ApproverDirectory;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.entity.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelPromotionHandler {
    private final PromotionCommandSupport support;
    private final ApproverDirectory approverDirectory;

    public CancelPromotionHandler(PromotionCommandSupport support, ApproverDirectory approverDirectory) {
        this.support = support;
        this.approverDirectory = approverDirectory;
    }

    @Transactional
    public PromotionResponse handle(CancelPromotionCommand command) {
        Promotion promotion = support.requirePromotion(command.promotionId());
        promotion.cancel(command.actingUser(), approverDirectory.isApprover(command.actingUser()), command.reason(), support.now());
        Promotion saved = support.saveAndRecord(promotion);
        support.updateTargetStatus(saved, EnvironmentState.CANCELLED);
        return PromotionResponseMapper.from(PromotionEntity.from(saved));
    }
}

