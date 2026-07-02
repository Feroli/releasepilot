package com.releasepilot.promotion.application.port;

public record WorkItem(
        String id,
        String title,
        String description,
        boolean breakingChangeRisk
) {
}

