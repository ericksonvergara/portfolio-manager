package com.portafolio_manager.backend.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SecurityConfigTest.TestController.class,
        properties = {
                "app.security.cors.allowed-origins=" + SecurityConfigTest.ALLOWED_ORIGIN,
                TestJwt.SECRET_PROPERTY
        }
)
@Import({SecurityConfig.class, SecurityConfigTest.TestController.class})
class SecurityConfigTest {

    static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldRejectRevokedBearerToken() throws Exception {
        String token = TestJwt.token(jwtEncoder, "revoked-id", TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
        when(revokedAccessTokenRepository.isRevoked("revoked-id")).thenReturn(true);

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectBearerTokenWithoutJwtId() throws Exception {
        String token = TestJwt.token(jwtEncoder, null, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIgnoreBearerTokenOnAuthEndpoints() throws Exception {
        mockMvc.perform(get("/api/auth/ping").header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-garbage"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldIgnoreBearerTokenOnPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/public/ping").header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-garbage"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminBearerToken() throws Exception {
        String token = TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenForBearerTokenWithoutAdminRole() throws Exception {
        String token = TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "USER");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectExpiredBearerToken() throws Exception {
        String token = TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().minusSeconds(120), "ADMIN");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldRejectBearerTokenFromAnotherIssuer() throws Exception {
        String token = TestJwt.token(jwtEncoder, "someone-else", Instant.now().plusSeconds(60), "ADMIN");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectBearerTokenSignedWithAnotherKey() throws Exception {
        SecretKey otherKey = new SecretKeySpec(
                "another-secret-that-is-also-32-bytes-long".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JwtEncoder foreignEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(otherKey));
        String token = TestJwt.token(foreignEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAnonymousRequestToPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedJsonForAnonymousRequestToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/protected/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/protected/ping"));
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousRequestToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenJsonWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = SecurityConfig.ADMIN_ROLE)
    void shouldAllowAdminRequestToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotCreateHttpSession() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldAllowCorsPreflightFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/admin/ping")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void shouldRejectCorsPreflightFromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/admin/ping")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldEncodePasswordsWithBcrypt() {
        String encoded = passwordEncoder.encode("Secret#1");

        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(passwordEncoder.matches("Secret#1", encoded)).isTrue();
    }

    @RestController
    static class TestController {

        @GetMapping({"/api/public/ping", "/api/auth/ping", "/api/protected/ping", "/api/admin/ping"})
        String ping() {
            return "pong";
        }
    }
}
