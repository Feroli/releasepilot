package com.releasepilot.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:edgecases;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class PromotionEdgeCasesApiTest {
    private static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("B2: omitting sourceEnvironment is a 400, not a 500 NPE")
    void missingSourceEnvironmentIsBadRequest() throws Exception {
        setup("edge-b2", "1.0.0", true);
        mockMvc.perform(post("/promotions").contentType(MediaType.APPLICATION_JSON).content("""
                {"applicationId":"edge-b2","version":"1.0.0","targetEnvironment":"STAGING","requestedBy":"u"}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("B1: invalid pagination (size=0, negative page/size) is a 400, not a 500")
    void invalidPaginationIsBadRequest() throws Exception {
        setup("edge-b1", "1.0.0", true);
        mockMvc.perform(get("/applications/edge-b1/promotions?page=0&size=0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/applications/edge-b1/promotions?page=-1&size=20")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/applications/edge-b1/promotions?page=0&size=-5")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("B1b: oversized page size is capped, not unbounded")
    void oversizedPageSizeIsCapped() throws Exception {
        setup("edge-b1b", "1.0.0", true);
        mockMvc.perform(get("/applications/edge-b1b/promotions?page=0&size=1000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    @DisplayName("G1: rollback and cancel require an approver")
    void rollbackAndCancelRequireApprover() throws Exception {
        setup("edge-g1", "1.0.0", true);
        String id = request("edge-g1", "1.0.0", "DEV", "STAGING");
        act(id, "approve", "{\"actingUser\":\"release-manager\"}", status().isOk());
        act(id, "rollback", "{\"actingUser\":\"intruder\",\"reason\":\"x\"}", status().isForbidden());
        act(id, "rollback", "{\"actingUser\":\"release-manager\",\"reason\":\"x\"}", status().isOk());

        String id2 = request("edge-g1", "1.0.0", "DEV", "STAGING");
        act(id2, "cancel", "{\"actingUser\":\"intruder\",\"reason\":\"x\"}", status().isForbidden());
        act(id2, "cancel", "{\"actingUser\":\"release-manager\",\"reason\":\"x\"}", status().isOk());
    }

    @Test
    @DisplayName("G2: status and history for an unknown application are 404")
    void unknownApplicationIsNotFound() throws Exception {
        mockMvc.perform(get("/applications/no-such-app/status")).andExpect(status().isNotFound());
        mockMvc.perform(get("/applications/no-such-app/promotions")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("G3: re-promoting a version to an already-completed target is rejected")
    void rePromotingCompletedTargetIsRejected() throws Exception {
        setup("edge-g3", "1.0.0", true);
        String id = request("edge-g3", "1.0.0", "DEV", "STAGING");
        act(id, "approve", "{\"actingUser\":\"release-manager\"}", status().isOk());
        act(id, "deployments", "{\"actingUser\":\"release-manager\"}", status().isOk());
        act(id, "complete", "{\"actingUser\":\"release-manager\"}", status().isOk());

        mockMvc.perform(post("/promotions").contentType(MediaType.APPLICATION_JSON).content("""
                {"applicationId":"edge-g3","version":"1.0.0","sourceEnvironment":"DEV","targetEnvironment":"STAGING","requestedBy":"u"}
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("TARGET_ENVIRONMENT_ALREADY_COMPLETED"));
    }

    @Test
    @DisplayName("G4: paged history exposes a stable content/totalElements/page/size envelope")
    void pagedHistoryHasStableEnvelope() throws Exception {
        setup("edge-g4", "1.0.0", true);
        request("edge-g4", "1.0.0", "DEV", "STAGING");
        mockMvc.perform(get("/applications/edge-g4/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    private void setup(String appId, String version, boolean devCompleted) throws Exception {
        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"%s\",\"name\":\"%s\"}".formatted(appId, appId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/applications/{id}/versions", appId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"%s\",\"devCompleted\":%s}".formatted(version, devCompleted)))
                .andExpect(status().isCreated());
    }

    private String request(String appId, String version, String source, String target) throws Exception {
        String response = mockMvc.perform(post("/promotions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"applicationId":"%s","version":"%s","sourceEnvironment":"%s","targetEnvironment":"%s","requestedBy":"u"}
                                """.formatted(appId, version, source, target)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private void act(String id, String command, String body, org.springframework.test.web.servlet.ResultMatcher expected) throws Exception {
        mockMvc.perform(post("/promotions/{id}/{command}", id, command)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(expected);
    }
}
