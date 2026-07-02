package com.releasepilot.promotion.infrastructure.messaging;

import com.releasepilot.promotion.infrastructure.persistence.OutboxEventEntity;
import com.releasepilot.promotion.infrastructure.persistence.OutboxEventJpaRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "releasepilot.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public OutboxPublisher(OutboxEventJpaRepository outboxRepository, RabbitTemplate rabbitTemplate, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${releasepilot.outbox.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        outboxRepository.findTop50ByPublishedAtIsNullOrderByOccurredAtAsc()
                .forEach(this::publish);
    }

    private void publish(OutboxEventEntity event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMessagingConfiguration.EXCHANGE, event.getEventType(), event.getPayload());
            event.markPublished(Instant.now(clock));
        } catch (AmqpException ex) {
            log.warn("outbox_publish_failed eventId={} eventType={}", event.getId(), event.getEventType());
            throw ex;
        }
    }
}
