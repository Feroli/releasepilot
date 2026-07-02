package com.releasepilot.web;

import com.releasepilot.catalog.application.CatalogService;
import com.releasepilot.promotion.domain.Environment;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationEntity;
import com.releasepilot.promotion.infrastructure.persistence.ApplicationVersionEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApplicationDto createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationEntity created = catalogService.createApplication(request.id(), request.name());
        return new ApplicationDto(created.getId(), created.getName());
    }

    @PostMapping("/{applicationId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    VersionDto registerVersion(
            @PathVariable String applicationId,
            @Valid @RequestBody RegisterVersionRequest request) {
        ApplicationVersionEntity created = catalogService.registerVersion(
                applicationId,
                request.version(),
                request.devCompleted());
        return new VersionDto(created.getId().toString(), created.getApplicationId(), created.getVersion());
    }

    @PostMapping("/{applicationId}/versions/{version}/environments/{environment}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markEnvironmentCompleted(
            @PathVariable String applicationId,
            @PathVariable String version,
            @PathVariable Environment environment) {
        catalogService.markEnvironmentCompleted(applicationId, version, environment);
    }

    public record CreateApplicationRequest(@NotBlank String id, @NotBlank String name) {
    }

    public record RegisterVersionRequest(@NotBlank String version, boolean devCompleted) {
    }

    public record ApplicationDto(String id, String name) {
    }

    public record VersionDto(String id, String applicationId, String version) {
    }
}

