package com.portafolio_manager.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    /**
     * Mirrors the public routes of SecurityConfig: every other operation requires a JWT.
     */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of("/api/public/", "/api/auth/");

    @Bean
    OpenAPI portfolioManagerOpenApi() {
        return new OpenAPI()
                // Relative URL: Swagger UI calls the API on the same origin it was loaded from (e.g. nginx on :8081).
                .servers(List.of(new Server().url("/")))
                .info(new Info()
                        .title("Portfolio Manager API")
                        .version("v1")
                        .description("""
                                API REST del portafolio.

                                **Autenticación:** obtén un access token con `POST /api/auth/login` y pulsa \
                                *Authorize* para enviarlo como `Bearer` en las rutas protegidas. \
                                El refresh token viaja en una cookie HttpOnly gestionada por el navegador."""))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token devuelto por /api/auth/login o /api/auth/refresh")));
    }

    @Bean
    OpenApiCustomizer protectedOperationsCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (isPublic(path)) {
                return;
            }
            pathItem.readOperations().forEach(operation ->
                    operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH)));
        });
    }

    private static boolean isPublic(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
