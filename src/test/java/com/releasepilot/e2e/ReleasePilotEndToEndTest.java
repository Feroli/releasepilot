package com.releasepilot.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReleasePilotEndToEndTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void runsPromotionWorkflowThroughHttpApi() {
        ResponseEntity<JsonNode> application = post("/applications", Map.of(
                "id", "e2e-api",
                "name", "E2E API"));
        assertThat(application.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> version = post("/applications/e2e-api/versions", Map.of(
                "version", "1.0.0",
                "devCompleted", true));
        assertThat(version.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> requested = post("/promotions", Map.of(
                "applicationId", "e2e-api",
                "version", "1.0.0",
                "sourceEnvironment", "DEV",
                "targetEnvironment", "STAGING",
                "requestedBy", "fernando"));
        assertThat(requested.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(requested.getBody().path("status").asText()).isEqualTo("REQUESTED");
        UUID promotionId = UUID.fromString(requested.getBody().path("id").asText());

        ResponseEntity<JsonNode> approved = post("/promotions/%s/approve".formatted(promotionId), Map.of(
                "actingUser", "release-manager"));
        assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approved.getBody().path("status").asText()).isEqualTo("APPROVED");

        ResponseEntity<JsonNode> deploying = post("/promotions/%s/deployments".formatted(promotionId), Map.of(
                "actingUser", "release-manager"));
        assertThat(deploying.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deploying.getBody().path("status").asText()).isEqualTo("DEPLOYING");
        assertThat(deploying.getBody().path("deploymentRef").asText()).startsWith("dep-");

        ResponseEntity<JsonNode> completed = post("/promotions/%s/complete".formatted(promotionId), Map.of(
                "actingUser", "release-manager"));
        assertThat(completed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completed.getBody().path("status").asText()).isEqualTo("COMPLETED");

        ResponseEntity<JsonNode> detail = restTemplate.getForEntity("/promotions/%s".formatted(promotionId), JsonNode.class);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detail.getBody().path("promotion").path("status").asText()).isEqualTo("COMPLETED");
        assertThat(detail.getBody().path("history")).hasSize(4);

        ResponseEntity<JsonNode> status = restTemplate.getForEntity("/applications/e2e-api/status", JsonNode.class);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(status.getBody().path("environments").get(1).path("environment").asText()).isEqualTo("STAGING");
        assertThat(status.getBody().path("environments").get(1).path("state").asText()).isEqualTo("COMPLETED");

        ResponseEntity<JsonNode> immutable = post("/promotions/%s/cancel".formatted(promotionId), Map.of(
                "actingUser", "release-manager",
                "reason", "late cancellation"));
        assertThat(immutable.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(immutable.getBody().path("title").asText()).isEqualTo("PROMOTION_IMMUTABLE");
    }

    private ResponseEntity<JsonNode> post(String path, Map<String, Object> body) {
        return restTemplate.postForEntity(path, body, JsonNode.class);
    }
}
