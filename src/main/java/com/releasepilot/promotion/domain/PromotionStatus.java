package com.releasepilot.promotion.domain;

import java.util.Set;

public enum PromotionStatus {
    REQUESTED,
    APPROVED,
    DEPLOYING,
    COMPLETED,
    ROLLED_BACK,
    CANCELLED;

    private static final Set<PromotionStatus> TERMINAL = Set.of(COMPLETED, ROLLED_BACK, CANCELLED);
    private static final Set<PromotionStatus> IN_PROGRESS = Set.of(REQUESTED, APPROVED, DEPLOYING);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean isInProgress() {
        return IN_PROGRESS.contains(this);
    }
}

