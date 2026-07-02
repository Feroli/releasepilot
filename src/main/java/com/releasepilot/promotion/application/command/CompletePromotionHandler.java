package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.CompletePromotionCommand;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompletePromotionHandler {
    private final PromotionCommandSupport support;

    public CompletePromotionHandler(PromotionCommandSupport support) {
        this.support = support;
    }

    @Transactional
    public PromotionResponse handle(CompletePromotionCommand command) {
        Promotion promotion = support.requirePromotion(command.promotionId());
        promotion.complete(command.actingUser(), support.now());
        Promotion saved = support.saveAndRecord(promotion);
        support.updateTargetStatus(saved, EnvironmentState.COMPLETED);
        return PromotionResponseMapper.from(PromotionEntity.from(saved));
    }
}

