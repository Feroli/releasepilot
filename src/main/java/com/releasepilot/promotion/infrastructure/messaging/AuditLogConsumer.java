package com.releasepilot.promotion.infrastructure.messaging;

import com.releasepilot.promotion.application.event.EventEnvelope;
import com.releasepilot.promotion.application.event.EventJson;
import com.releasepilot.promotion.infrastructure.persistence.AuditLogEntity;
import com.releasepilot.promotion.infrastructure.persistence.AuditLogJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditLogConsumer {
    private final AuditLogJpaRepository auditLogRepository;
    private final EventJson eventJson;
    private final Clock clock;

    public AuditLogConsumer(AuditLogJpaRepository auditLogRepository, EventJson eventJson, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.eventJson = eventJson;
        this.clock = clock;
    }

    @RabbitListener(queues = RabbitMessagingConfiguration.AUDIT_QUEUE)
    @Transactional
    public void consume(String payload) {
        EventEnvelope event = eventJson.read(payload);
        persist(event);
    }

    public void persist(EventEnvelope event) {
        if (auditLogRepository.existsByEventId(event.eventId())) {
            return;
        }
        auditLogRepository.save(new AuditLogEntity(
                UUID.randomUUID(),
                event.eventId(),
                event.promotionId(),
                event.eventType(),
                event.actingUser(),
                event.occurredAt(),
                Instant.now(clock)));
    }
}

