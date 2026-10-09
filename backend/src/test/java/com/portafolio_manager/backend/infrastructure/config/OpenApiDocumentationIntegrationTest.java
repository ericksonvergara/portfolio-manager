package com.portafolio_manager.backend.infrastructure.config;

import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPublishOpenApiSpecWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Portfolio Manager API"))
                .andExpect(jsonPath("$.servers[0].url").value("/"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists());
    }

    @Test
    void shouldDocumentAuthenticationEndpoints() throws Exception {
        mockMvc.perform(get("/api/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary").value("Iniciar sesión"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.requestBody.content['application/json'].schema['$ref']")
                        .value("#/components/schemas/LoginRequest"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/auth/refresh'].post.parameters[*].in").value(hasItem("cookie")))
                .andExpect(jsonPath("$.paths['/api/auth/logout'].post.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/logout'].post.security[0].bearerAuth").exists());
    }

    @Test
    void shouldServeSwaggerUiWithoutAuthentication() throws Exception {
        String location = mockMvc.perform(get("/api/docs/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, startsWith("/api/docs/")))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.LOCATION);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));

        mockMvc.perform(get("/api/docs/openapi/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/api/docs/openapi"));
    }
}
