package com.releasepilot.promotion.application.port;

import com.releasepilot.promotion.domain.Promotion;

public interface DeploymentPort {
    String startDeployment(Promotion promotion, String actingUser);
}

