package com.releasepilot.releasenotes.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.releasepilot.promotion.application.event.EventEnvelope;
import com.releasepilot.promotion.application.port.IssueTrackerPort;
import com.releasepilot.promotion.application.port.WorkItem;
import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.domain.PromotionDomainEvent;
import com.releasepilot.promotion.infrastructure.persistence.ReleaseNoteDraftJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ReleaseNotesAgentTest {
    @Autowired
    private ReleaseNotesAgent agent;
    @Autowired
    private ReleaseNoteDraftJpaRepository draftRepository;

    @Test
    void draftsReleaseNotesWithBreakingChangeReview() {
        UUID promotionId = UUID.randomUUID();
        EventEnvelope event = new EventEnvelope(
                UUID.randomUUID(),
                PromotionDomainEvent.APPROVED,
                promotionId,
                "payments-api",
                UUID.randomUUID(),
                "1.4.0",
                Environment.DEV,
                Environment.STAGING,
                "release-manager",
                Instant.parse("2026-07-02T15:00:00Z"),
                Map.of());

        agent.draftFor(event);

        assertThat(draftRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId))
                .singleElement()
                .satisfies(draft -> {
                    assertThat(draft.getDraft()).contains("Release notes for payments-api 1.4.0");
                    assertThat(draft.getDraft()).contains("Breaking-change review needed");
                    assertThat(draft.getStatus()).isEqualTo("DRAFT");
                });
    }

    @TestConfiguration
    static class AgentTestConfiguration {
        @Bean
        @Primary
        IssueTrackerPort issueTrackerPort() {
            return promotionId -> List.of(
                    new WorkItem("REL-1", "Add workflow", "Promotion workflow", false),
                    new WorkItem("REL-2", "Change API contract", "Contract changes require review", true));
        }
    }
}

