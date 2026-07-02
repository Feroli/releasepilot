package com.releasepilot.promotion.infrastructure.messaging;

import com.releasepilot.promotion.application.event.EventEnvelope;
import com.releasepilot.promotion.application.event.EventJson;
import com.releasepilot.promotion.application.port.NotificationPort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private final EventJson eventJson;
    private final NotificationPort notificationPort;

    public NotificationConsumer(EventJson eventJson, NotificationPort notificationPort) {
        this.eventJson = eventJson;
        this.notificationPort = notificationPort;
    }

    @RabbitListener(queues = RabbitMessagingConfiguration.NOTIFICATION_QUEUE)
    public void consume(String payload) {
        notificationPort.notifyTerminalState(eventJson.read(payload));
    }
}

