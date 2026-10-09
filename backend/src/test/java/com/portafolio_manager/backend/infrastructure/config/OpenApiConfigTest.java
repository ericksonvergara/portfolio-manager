package com.portafolio_manager.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void shouldRequireBearerTokenOnlyOnProtectedOperations() {
        Operation adminOperation = new Operation();
        Operation authOperation = new Operation();
        Operation publicOperation = new Operation();
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/api/admin/projects", new PathItem().get(adminOperation))
                .addPathItem("/api/auth/login", new PathItem().post(authOperation))
                .addPathItem("/api/public/projects", new PathItem().get(publicOperation)));

        config.protectedOperationsCustomizer().customise(openApi);

        assertThat(adminOperation.getSecurity())
                .containsExactly(new SecurityRequirement().addList(OpenApiConfig.BEARER_AUTH));
        assertThat(authOperation.getSecurity()).isNull();
        assertThat(publicOperation.getSecurity()).isNull();
    }
}
