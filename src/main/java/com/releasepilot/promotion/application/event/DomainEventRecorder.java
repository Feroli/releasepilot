package com.releasepilot.promotion.application.event;

import com.releasepilot.promotion.domain.PromotionDomainEvent;
import com.releasepilot.promotion.infrastructure.persistence.OutboxEventEntity;
import com.releasepilot.promotion.infrastructure.persistence.OutboxEventJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEventEntity;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEventJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DomainEventRecorder {
    private final PromotionEventJpaRepository eventRepository;
    private final OutboxEventJpaRepository outboxRepository;
    private final EventJson eventJson;

    public DomainEventRecorder(
            PromotionEventJpaRepository eventRepository,
            OutboxEventJpaRepository outboxRepository,
            EventJson eventJson) {
        this.eventRepository = eventRepository;
        this.outboxRepository = outboxRepository;
        this.eventJson = eventJson;
    }

    public void record(List<PromotionDomainEvent> events) {
        events.forEach(event -> {
            EventEnvelope envelope = EventEnvelope.from(event);
            String json = eventJson.write(envelope);
            eventRepository.save(new PromotionEventEntity(
                    event.eventId(),
                    event.promotionId(),
                    event.eventType(),
                    event.actingUser(),
                    json,
                    event.occurredAt()));
            outboxRepository.save(new OutboxEventEntity(
                    event.eventId(),
                    "Promotion",
                    event.promotionId(),
                    event.eventType(),
                    json,
                    event.occurredAt()));
        });
    }
}

