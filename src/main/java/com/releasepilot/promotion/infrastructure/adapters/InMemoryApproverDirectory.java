package com.releasepilot.promotion.infrastructure.adapters;

import com.releasepilot.promotion.application.port.ApproverDirectory;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InMemoryApproverDirectory implements ApproverDirectory {
    private static final Set<String> APPROVERS = Set.of("release-manager", "lead", "admin");

    @Override
    public boolean isApprover(String user) {
        return APPROVERS.contains(user);
    }
}

