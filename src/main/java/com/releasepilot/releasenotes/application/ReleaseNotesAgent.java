package com.releasepilot.releasenotes.application;

import com.releasepilot.promotion.application.event.EventEnvelope;
import com.releasepilot.promotion.application.port.IssueTrackerPort;
import com.releasepilot.promotion.application.port.WorkItem;
import com.releasepilot.promotion.infrastructure.persistence.ReleaseNoteDraftEntity;
import com.releasepilot.promotion.infrastructure.persistence.ReleaseNoteDraftJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReleaseNotesAgent {
    private final IssueTrackerPort issueTrackerPort;
    private final ReleaseNoteDraftJpaRepository draftRepository;
    private final Clock clock;

    public ReleaseNotesAgent(IssueTrackerPort issueTrackerPort, ReleaseNoteDraftJpaRepository draftRepository, Clock clock) {
        this.issueTrackerPort = issueTrackerPort;
        this.draftRepository = draftRepository;
        this.clock = clock;
    }

    @Transactional
    public ReleaseNoteDraftEntity draftFor(EventEnvelope event) {
        List<WorkItem> workItems = issueTrackerPort.getWorkItems(event.promotionId());
        String draft = buildDraft(event, workItems);
        return draftRepository.save(new ReleaseNoteDraftEntity(
                UUID.randomUUID(),
                event.promotionId(),
                draft,
                "DRAFT",
                Instant.now(clock)));
    }

    private String buildDraft(EventEnvelope event, List<WorkItem> workItems) {
        StringBuilder builder = new StringBuilder();
        builder.append("Release notes for ")
                .append(event.applicationId())
                .append(" ")
                .append(event.version())
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Changes:")
                .append(System.lineSeparator());
        workItems.forEach(item -> builder.append("- ")
                .append(item.id())
                .append(": ")
                .append(item.title())
                .append(System.lineSeparator()));
        List<WorkItem> breaking = workItems.stream()
                .filter(WorkItem::breakingChangeRisk)
                .toList();
        if (!breaking.isEmpty()) {
            builder.append(System.lineSeparator()).append("Breaking-change review needed:").append(System.lineSeparator());
            breaking.forEach(item -> builder.append("- ")
                    .append(item.id())
                    .append(": ")
                    .append(item.description())
                    .append(System.lineSeparator()));
        }
        return builder.toString();
    }
}

