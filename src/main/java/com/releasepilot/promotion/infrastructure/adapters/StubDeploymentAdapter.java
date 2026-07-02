package com.releasepilot.promotion.infrastructure.adapters;

import com.releasepilot.promotion.application.port.DeploymentPort;
import com.releasepilot.promotion.domain.Promotion;
import org.springframework.stereotype.Component;

@Component
public class StubDeploymentAdapter implements DeploymentPort {
    @Override
    public String startDeployment(Promotion promotion, String actingUser) {
        return "dep-" + promotion.id().toString().substring(0, 8);
    }
}

