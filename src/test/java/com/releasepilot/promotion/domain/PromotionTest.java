package com.releasepilot.promotion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PromotionTest {
    private static final Instant NOW = Instant.parse("2026-07-02T15:00:00Z");
    private static final UUID PROMOTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VERSION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void requestsValidPromotionAndRecordsEvent() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);

        assertThat(promotion.status()).isEqualTo(PromotionStatus.REQUESTED);
        assertThat(promotion.sourceEnvironment()).isEqualTo(Environment.DEV);
        assertThat(promotion.targetEnvironment()).isEqualTo(Environment.STAGING);
        assertThat(promotion.pullRecordedEvents())
                .extracting(PromotionDomainEvent::eventType)
                .containsExactly(PromotionDomainEvent.REQUESTED);
    }

    @Test
    void requestsStagingToProductionWhenStagingCompleted() {
        Promotion promotion = Promotion.request(
                PROMOTION_ID,
                "payments",
                VERSION_ID,
                "1.0.0",
                Environment.STAGING,
                Environment.PRODUCTION,
                "requester",
                new PromotionEligibility(true, false, false),
                NOW);

        assertThat(promotion.status()).isEqualTo(PromotionStatus.REQUESTED);
    }

    @Test
    void rejectsSkippedEnvironment() {
        assertDomainError(
                () -> request(Environment.DEV, Environment.PRODUCTION),
                DomainErrorCode.ENVIRONMENT_SKIPPED);
    }

    @Test
    void rejectsIncompleteSourceEnvironment() {
        assertDomainError(
                () -> Promotion.request(
                        PROMOTION_ID,
                        "payments",
                        VERSION_ID,
                        "1.0.0",
                        Environment.DEV,
                        Environment.STAGING,
                        "requester",
                        new PromotionEligibility(false, false, false),
                        NOW),
                DomainErrorCode.PREVIOUS_ENVIRONMENT_INCOMPLETE);
    }

    @Test
    void rejectsActivePromotionForSameTarget() {
        assertDomainError(
                () -> Promotion.request(
                        PROMOTION_ID,
                        "payments",
                        VERSION_ID,
                        "1.0.0",
                        Environment.DEV,
                        Environment.STAGING,
                        "requester",
                        new PromotionEligibility(true, true, false),
                        NOW),
                DomainErrorCode.PROMOTION_ALREADY_IN_PROGRESS);
    }

    @Test
    void approvesByApproverAndRecordsEvent() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);
        promotion.pullRecordedEvents();

        promotion.approve("release-manager", true, NOW.plusSeconds(1));

        assertThat(promotion.status()).isEqualTo(PromotionStatus.APPROVED);
        assertThat(promotion.approvedBy()).isEqualTo("release-manager");
        assertThat(promotion.pullRecordedEvents())
                .extracting(PromotionDomainEvent::eventType)
                .containsExactly(PromotionDomainEvent.APPROVED);
    }

    @Test
    void rejectsApprovalByNonApprover() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);
        promotion.pullRecordedEvents();

        assertDomainError(
                () -> promotion.approve("developer", false, NOW.plusSeconds(1)),
                DomainErrorCode.APPROVER_REQUIRED);
        assertThat(promotion.pullRecordedEvents()).isEmpty();
    }

    @Test
    void rejectsDeploymentBeforeApproval() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);
        promotion.pullRecordedEvents();

        assertDomainError(
                () -> promotion.startDeployment("release-manager", "dep-1", NOW.plusSeconds(1)),
                DomainErrorCode.INVALID_PROMOTION_STATE);
        assertThat(promotion.pullRecordedEvents()).isEmpty();
    }

    @Test
    void completesDeploymentAndBecomesImmutable() {
        Promotion promotion = approvedPromotion();
        promotion.startDeployment("release-manager", "dep-1", NOW.plusSeconds(2));
        promotion.complete("release-manager", NOW.plusSeconds(3));

        assertThat(promotion.status()).isEqualTo(PromotionStatus.COMPLETED);
        assertThat(promotion.terminalAt()).isEqualTo(NOW.plusSeconds(3));
        assertDomainError(
                () -> promotion.cancel("release-manager", true, "too late", NOW.plusSeconds(4)),
                DomainErrorCode.PROMOTION_IMMUTABLE);
    }

    @Test
    void rollsBackApprovedPromotion() {
        Promotion promotion = approvedPromotion();

        promotion.rollBack("release-manager", true, "failed checks", NOW.plusSeconds(2));

        assertThat(promotion.status()).isEqualTo(PromotionStatus.ROLLED_BACK);
        assertThat(promotion.pullRecordedEvents())
                .extracting(PromotionDomainEvent::eventType)
                .containsExactly(PromotionDomainEvent.APPROVED, PromotionDomainEvent.ROLLED_BACK);
    }

    @Test
    void cancelsRequestedPromotionAndBecomesImmutable() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);
        promotion.pullRecordedEvents();

        promotion.cancel("requester", true, "superseded", NOW.plusSeconds(1));

        assertThat(promotion.status()).isEqualTo(PromotionStatus.CANCELLED);
        assertDomainError(
                () -> promotion.approve("release-manager", true, NOW.plusSeconds(2)),
                DomainErrorCode.PROMOTION_IMMUTABLE);
    }

    private static Promotion request(Environment source, Environment target) {
        return Promotion.request(
                PROMOTION_ID,
                "payments",
                VERSION_ID,
                "1.0.0",
                source,
                target,
                "requester",
                new PromotionEligibility(true, false, false),
                NOW);
    }

    private static Promotion approvedPromotion() {
        Promotion promotion = request(Environment.DEV, Environment.STAGING);
        promotion.pullRecordedEvents();
        promotion.approve("release-manager", true, NOW.plusSeconds(1));
        return promotion;
    }

    private static void assertDomainError(Runnable action, DomainErrorCode code) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(DomainException.class, ex ->
                        assertThat(ex.code()).isEqualTo(code));
    }
}

