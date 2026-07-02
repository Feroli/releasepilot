package com.releasepilot.promotion.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Environment {
    DEV,
    STAGING,
    PRODUCTION;

    public Optional<Environment> next() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[nextOrdinal]);
    }

    public static Environment parse(String value) {
        return Arrays.stream(values())
                .filter(environment -> environment.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        DomainErrorCode.INVALID_ENVIRONMENT,
                        "Unknown environment: " + value));
    }
}

