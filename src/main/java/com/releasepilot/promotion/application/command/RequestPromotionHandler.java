package com.releasepilot.promotion.application.command;

import com.releasepilot.promotion.application.command.Commands.RequestPromotionCommand;
import com.releasepilot.promotion.application.query.PromotionReadModels.PromotionResponse;
import com.releasepilot.promotion.application.query.PromotionResponseMapper;
import com.releasepilot.promotion.domain.Promotion;
import com.releasepilot.promotion.infrastructure.persistence.entity.ApplicationVersionEntity;
import com.releasepilot.promotion.infrastructure.persistence.entity.PromotionEntity;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPromotionHandler {
    private final PromotionCommandSupport support;

    public RequestPromotionHandler(PromotionCommandSupport support) {
        this.support = support;
    }

    @Transactional
    public PromotionResponse handle(RequestPromotionCommand command) {
        ApplicationVersionEntity version = support.requireVersion(command.applicationId(), command.version());
        Promotion promotion = Promotion.request(
                UUID.randomUUID(),
                command.applicationId(),
                version.getId(),
                version.getVersion(),
                command.sourceEnvironment(),
                command.targetEnvironment(),
                command.requestedBy(),
                support.eligibility(command.applicationId(), version.getId(), command.sourceEnvironment(), command.targetEnvironment()),
                support.now());
        return PromotionResponseMapper.from(PromotionEntity.from(support.saveAndRecord(promotion)));
    }
}

