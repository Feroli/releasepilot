package com.releasepilot.promotion.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventJson {
    private final ObjectMapper objectMapper;

    public EventJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(EventEnvelope event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize event", ex);
        }
    }

    public EventEnvelope read(String json) {
        try {
            return objectMapper.readValue(json, EventEnvelope.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to deserialize event", ex);
        }
    }
}

