package com.releasepilot.promotion.application.port;

import com.releasepilot.promotion.application.event.EventEnvelope;

public interface NotificationPort {
    void notifyTerminalState(EventEnvelope event);
}

