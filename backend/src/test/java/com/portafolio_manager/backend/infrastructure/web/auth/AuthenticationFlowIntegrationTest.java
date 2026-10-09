package com.portafolio_manager.backend.infrastructure.web.auth;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        TestJwt.SECRET_PROPERTY,
        "app.bootstrap.admin.email=" + AuthenticationFlowIntegrationTest.ADMIN_EMAIL,
        "app.bootstrap.admin.password=" + AuthenticationFlowIntegrationTest.ADMIN_PASSWORD
})
@AutoConfigureMockMvc
@Testcontainers
class AuthenticationFlowIntegrationTest {

    static final String ADMIN_EMAIL = "admin@portfolio.test";
    static final String ADMIN_PASSWORD = "Secret#123";
    private static final String REFRESH_COOKIE = "refresh_token";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoginRefreshDetectReuseAndLogout() throws Exception {
        mockMvc.perform(login(ADMIN_EMAIL, "Wrong#123")).andExpect(status().isUnauthorized());

        MvcResult firstLogin = mockMvc.perform(login(ADMIN_EMAIL.toUpperCase(), ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = JsonPath.read(firstLogin.getResponse().getContentAsString(), "$.accessToken");
        Cookie firstRefresh = firstLogin.getResponse().getCookie(REFRESH_COOKIE);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT last_login_at FROM users WHERE email = ?", Timestamp.class, ADMIN_EMAIL
        )).isNotNull();

        // The access token gets through security: an unknown route answers 404 instead of 401.
        mockMvc.perform(get("/api/admin/unknown").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/unknown")).andExpect(status().isUnauthorized());

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh))
                .andExpect(status().isOk())
                .andReturn();
        Cookie secondRefresh = refreshed.getResponse().getCookie(REFRESH_COOKIE);
        assertThat(secondRefresh.getValue()).isNotEqualTo(firstRefresh.getValue());

        // Reusing a rotated token is treated as theft and revokes every session of the user.
        mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(secondRefresh)).andExpect(status().isUnauthorized());

        MvcResult thirdLogin = mockMvc.perform(login(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andReturn();
        String thirdAccessToken = JsonPath.read(thirdLogin.getResponse().getContentAsString(), "$.accessToken");
        Cookie thirdRefresh = thirdLogin.getResponse().getCookie(REFRESH_COOKIE);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(thirdRefresh)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + thirdAccessToken))
                .andExpect(status().isNoContent());

        // HU009: after logout neither token grants access any more.
        mockMvc.perform(post("/api/auth/refresh").cookie(thirdRefresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/unknown").header(HttpHeaders.AUTHORIZATION, "Bearer " + thirdAccessToken))
                .andExpect(status().isUnauthorized());

        // Logging out again with the now revoked tokens still succeeds.
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(thirdRefresh)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + thirdAccessToken))
                .andExpect(status().isNoContent());
    }

    private static org.springframework.test.web.servlet.RequestBuilder login(String email, String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
    }
}
