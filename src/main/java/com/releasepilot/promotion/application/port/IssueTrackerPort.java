package com.releasepilot.promotion.application.port;

import java.util.List;
import java.util.UUID;

public interface IssueTrackerPort {
    List<WorkItem> getWorkItems(UUID promotionId);
}

