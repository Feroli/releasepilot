package com.releasepilot.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.releasepilot.promotion.application.event.EventJson;
import com.releasepilot.promotion.infrastructure.messaging.AuditLogConsumer;
import com.releasepilot.promotion.infrastructure.persistence.AuditLogJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.OutboxEventJpaRepository;
import com.releasepilot.promotion.infrastructure.persistence.PromotionEventJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PromotionWorkflowApiTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PromotionEventJpaRepository eventRepository;
    @Autowired
    private OutboxEventJpaRepository outboxRepository;
    @Autowired
    private AuditLogJpaRepository auditLogRepository;
    @Autowired
    private AuditLogConsumer auditLogConsumer;
    @Autowired
    private EventJson eventJson;

    @Test
    void completesPromotionWorkflowAndReturnsReadModels() throws Exception {
        createApplication("payments-api");
        registerVersion("payments-api", "1.4.0", true);

        String requestBody = """
                {
                  "applicationId": "payments-api",
                  "version": "1.4.0",
                  "sourceEnvironment": "DEV",
                  "targetEnvironment": "STAGING",
                  "requestedBy": "fernando"
                }
                """;
        String response = mockMvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID promotionId = UUID.fromString(com.jayway.jsonpath.JsonPath.read(response, "$.id"));

        mockMvc.perform(post("/promotions/{promotionId}/approve", promotionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actingUser": "release-manager"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/promotions/{promotionId}/deployments", promotionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actingUser": "release-manager"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPLOYING"))
                .andExpect(jsonPath("$.deploymentRef").exists());

        mockMvc.perform(post("/promotions/{promotionId}/complete", promotionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actingUser": "release-manager"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/promotions/{promotionId}", promotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotion.status").value("COMPLETED"))
                .andExpect(jsonPath("$.history.length()").value(4));

        mockMvc.perform(get("/applications/payments-api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environments[0].environment").value("DEV"))
                .andExpect(jsonPath("$.environments[0].state").value("COMPLETED"))
                .andExpect(jsonPath("$.environments[1].environment").value("STAGING"))
                .andExpect(jsonPath("$.environments[1].state").value("COMPLETED"));

        mockMvc.perform(get("/applications/payments-api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        assertThat(eventRepository.findByPromotionIdOrderByOccurredAtAsc(promotionId)).hasSize(4);
        assertThat(outboxRepository.findAll()).hasSize(4);

        outboxRepository.findAll().forEach(event -> auditLogConsumer.persist(eventJson.read(event.getPayload())));

        mockMvc.perform(get("/promotions/{promotionId}/audit-log", promotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        assertThat(auditLogRepository.findByPromotionIdOrderByOccurredAtAsc(promotionId)).hasSize(4);
    }

    @Test
    void rejectsInvalidPromotionRequestsWithDomainErrors() throws Exception {
        createApplication("search-api");
        registerVersion("search-api", "2.0.0", true);

        mockMvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "search-api",
                                  "version": "2.0.0",
                                  "sourceEnvironment": "DEV",
                                  "targetEnvironment": "PRODUCTION",
                                  "requestedBy": "fernando"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("ENVIRONMENT_SKIPPED"));

        String valid = mockMvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "search-api",
                                  "version": "2.0.0",
                                  "sourceEnvironment": "DEV",
                                  "targetEnvironment": "STAGING",
                                  "requestedBy": "fernando"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID promotionId = UUID.fromString(com.jayway.jsonpath.JsonPath.read(valid, "$.id"));

        mockMvc.perform(post("/promotions/{promotionId}/approve", promotionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actingUser": "developer"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("APPROVER_REQUIRED"));

        mockMvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "search-api",
                                  "version": "2.0.0",
                                  "sourceEnvironment": "DEV",
                                  "targetEnvironment": "STAGING",
                                  "requestedBy": "fernando"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("PROMOTION_ALREADY_IN_PROGRESS"));
    }

    private void createApplication(String id) throws Exception {
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "%s", "name": "%s"}
                                """.formatted(id, id)))
                .andExpect(status().isCreated());
    }

    private void registerVersion(String applicationId, String version, boolean devCompleted) throws Exception {
        mockMvc.perform(post("/applications/{applicationId}/versions", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version": "%s", "devCompleted": %s}
                                """.formatted(version, devCompleted)))
                .andExpect(status().isCreated());
    }
}

