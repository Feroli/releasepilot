package com.releasepilot.promotion.infrastructure.adapters;

import com.releasepilot.promotion.application.event.EventEnvelope;
import com.releasepilot.promotion.application.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationAdapter implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void notifyTerminalState(EventEnvelope event) {
        log.info("promotion_terminal_notification promotionId={} eventType={} actingUser={}",
                event.promotionId(),
                event.eventType(),
                event.actingUser());
    }
}

