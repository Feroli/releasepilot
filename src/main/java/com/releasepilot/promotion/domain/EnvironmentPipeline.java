package com.releasepilot.promotion.domain;

public final class EnvironmentPipeline {
    public void requireNextStep(Environment source, Environment target) {
        Environment expected = source.next()
                .orElseThrow(() -> new DomainException(
                        DomainErrorCode.INVALID_ENVIRONMENT,
                        "No target environment exists after " + source));
        if (expected != target) {
            throw new DomainException(
                    DomainErrorCode.ENVIRONMENT_SKIPPED,
                    "Promotion must move exactly one environment step");
        }
    }
}

