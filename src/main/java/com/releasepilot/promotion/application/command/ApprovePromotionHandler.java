package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.ApprovePromotionCommand;
import com.releasepilot.promotion.application.port.ApproverDirectory;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovePromotionHandler {
    private final PromotionCommandSupport support;
    private final ApproverDirectory approverDirectory;

    public ApprovePromotionHandler(PromotionCommandSupport support, ApproverDirectory approverDirectory) {
        this.support = support;
        this.approverDirectory = approverDirectory;
    }

    @Transactional
    public PromotionResponse handle(ApprovePromotionCommand command) {
        Promotion promotion = support.requirePromotion(command.promotionId());
        promotion.approve(command.actingUser(), approverDirectory.isApprover(command.actingUser()), support.now());
        return PromotionResponseMapper.from(PromotionEntity.from(support.saveAndRecord(promotion)));
    }
}

