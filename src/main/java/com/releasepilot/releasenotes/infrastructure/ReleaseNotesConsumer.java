package com.releasepilot.releasenotes.infrastructure;

import com.releasepilot.promotion.application.event.EventJson;
import com.releasepilot.promotion.infrastructure.messaging.RabbitMessagingConfiguration;
import com.releasepilot.releasenotes.application.ReleaseNotesAgent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReleaseNotesConsumer {
    private final EventJson eventJson;
    private final ReleaseNotesAgent agent;

    public ReleaseNotesConsumer(EventJson eventJson, ReleaseNotesAgent agent) {
        this.eventJson = eventJson;
        this.agent = agent;
    }

    @RabbitListener(queues = RabbitMessagingConfiguration.RELEASE_NOTES_QUEUE)
    public void consume(String payload) {
        agent.draftFor(eventJson.read(payload));
    }
}

