package com.releasepilot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.releasepilot.promotion.infrastructure.persistence.repository.AuditLogJpaRepository;
import com.releasepilot.promotion.infrastructure.messaging.OutboxPublisher;
import com.releasepilot.promotion.infrastructure.persistence.repository.ReleaseNoteDraftJpaRepository;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.rabbitmq.listener.simple.auto-startup=true",
        "spring.rabbitmq.listener.direct.auto-startup=true",
        "releasepilot.outbox.enabled=true",
        "releasepilot.outbox.fixed-delay-ms=60000"
})
class ReleasePilotContainersIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("releasepilot")
            .withUsername("releasepilot")
            .withPassword("releasepilot");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuditLogJpaRepository auditLogRepository;
    @Autowired
    private ReleaseNoteDraftJpaRepository draftRepository;
    @Autowired
    private OutboxPublisher outboxPublisher;

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Test
    void publishesDomainEventsToRabbitAndConsumesAuditAndReleaseNotes() throws Exception {
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "container-api", "name": "Container API"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/applications/container-api/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version": "3.0.0", "devCompleted": true}
                                """))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationId": "container-api",
                                  "version": "3.0.0",
                                  "sourceEnvironment": "DEV",
                                  "targetEnvironment": "STAGING",
                                  "requestedBy": "fernando"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID promotionId = UUID.fromString(com.jayway.jsonpath.JsonPath.read(response, "$.id"));

        mockMvc.perform(post("/promotions/{promotionId}/approve", promotionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actingUser": "release-manager"}
                                """))
                .andExpect(status().isOk());

        outboxPublisher.publishPending();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(auditLogRepository.findByPromotionIdOrderByOccurredAtAsc(promotionId)).hasSizeGreaterThanOrEqualTo(2);
            assertThat(draftRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId)).isNotEmpty();
        });

        mockMvc.perform(get("/promotions/{promotionId}/audit-log", promotionId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/promotions/{promotionId}/release-notes", promotionId))
                .andExpect(status().isOk());
    }
}
