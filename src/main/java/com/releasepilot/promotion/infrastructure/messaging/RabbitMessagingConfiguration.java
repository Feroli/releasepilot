package com.releasepilot.promotion.infrastructure.messaging;

import com.releasepilot.promotion.domain.PromotionDomainEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfiguration {
    public static final String EXCHANGE = "releasepilot.events";
    public static final String AUDIT_QUEUE = "releasepilot.audit";
    public static final String RELEASE_NOTES_QUEUE = "releasepilot.release-notes";
    public static final String NOTIFICATION_QUEUE = "releasepilot.notifications";

    @Bean
    TopicExchange releasePilotExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true);
    }

    @Bean
    Queue releaseNotesQueue() {
        return new Queue(RELEASE_NOTES_QUEUE, true);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    Binding auditBinding(TopicExchange releasePilotExchange, Queue auditQueue) {
        return BindingBuilder.bind(auditQueue).to(releasePilotExchange).with("#");
    }

    @Bean
    Binding releaseNotesBinding(TopicExchange releasePilotExchange, Queue releaseNotesQueue) {
        return BindingBuilder.bind(releaseNotesQueue).to(releasePilotExchange).with(PromotionDomainEvent.APPROVED);
    }

    @Bean
    Binding completedNotificationBinding(TopicExchange releasePilotExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(releasePilotExchange).with(PromotionDomainEvent.COMPLETED);
    }

    @Bean
    Binding rolledBackNotificationBinding(TopicExchange releasePilotExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(releasePilotExchange).with(PromotionDomainEvent.ROLLED_BACK);
    }

    @Bean
    Binding cancelledNotificationBinding(TopicExchange releasePilotExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(releasePilotExchange).with(PromotionDomainEvent.CANCELLED);
    }
}

