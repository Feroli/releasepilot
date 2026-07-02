package com.releasepilot.promotion.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EnvironmentPipelineTest {
    private final EnvironmentPipeline pipeline = new EnvironmentPipeline();

    @Test
    void acceptsValidNextEnvironmentSteps() {
        assertThatCode(() -> pipeline.requireNextStep(Environment.DEV, Environment.STAGING))
                .doesNotThrowAnyException();
        assertThatCode(() -> pipeline.requireNextStep(Environment.STAGING, Environment.PRODUCTION))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSkippedEnvironment() {
        assertThatThrownBy(() -> pipeline.requireNextStep(Environment.DEV, Environment.PRODUCTION))
                .isInstanceOfSatisfying(DomainException.class, ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.code())
                                .isEqualTo(DomainErrorCode.ENVIRONMENT_SKIPPED));
    }

    @Test
    void rejectsPromotionBeyondProduction() {
        assertThatThrownBy(() -> pipeline.requireNextStep(Environment.PRODUCTION, Environment.PRODUCTION))
                .isInstanceOfSatisfying(DomainException.class, ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.code())
                                .isEqualTo(DomainErrorCode.INVALID_ENVIRONMENT));
    }
}

