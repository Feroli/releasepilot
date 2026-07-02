package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.RollbackPromotionCommand;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.entity.EnvironmentState;
import com.releasepilot.promotion.infrastructure.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RollbackPromotionHandler {
    private final PromotionCommandSupport support;

    public RollbackPromotionHandler(PromotionCommandSupport support) {
        this.support = support;
    }

    @Transactional
    public PromotionResponse handle(RollbackPromotionCommand command) {
        Promotion promotion = support.requirePromotion(command.promotionId());
        promotion.rollBack(command.actingUser(), command.reason(), support.now());
        Promotion saved = support.saveAndRecord(promotion);
        support.updateTargetStatus(saved, EnvironmentState.ROLLED_BACK);
        return PromotionResponseMapper.from(PromotionEntity.from(saved));
    }
}

