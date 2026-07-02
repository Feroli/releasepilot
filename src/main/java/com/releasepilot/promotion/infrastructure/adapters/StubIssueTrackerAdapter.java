package com.releasepilot.promotion.infrastructure.adapters;

import com.releasepilot.promotion.application.port.IssueTrackerPort;
import com.releasepilot.promotion.application.port.WorkItem;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StubIssueTrackerAdapter implements IssueTrackerPort {
    @Override
    public List<WorkItem> getWorkItems(UUID promotionId) {
        String suffix = promotionId.toString().substring(0, 8);
        return List.of(
                new WorkItem("REL-" + suffix + "-1", "Add promotion workflow", "Command API and state machine changes", false),
                new WorkItem("REL-" + suffix + "-2", "Update deployment contracts", "Deployment adapter contract changed", true));
    }
}

