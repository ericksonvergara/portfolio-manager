package com.portafolio_manager.backend.infrastructure.web.contact;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.application.port.out.contact.ContactNotifierPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contact form and inbox (HU006) against the real schema. Every test uses its own client IP, because
 * the rate limiter lives as long as the Spring context shared by the integration tests.
 */
@SpringBootTest(properties = {TestJwt.SECRET_PROPERTY, "app.contact.max-messages=3"})
@AutoConfigureMockMvc
class ContactIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ContactNotifierPort notifier;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM contact_messages");
        jdbcTemplate.update("DELETE FROM profiles");
    }

    @Test
    void shouldSaveTheMessageNotifyTheOwnerAndShowItInTheInbox() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO profiles (full_name, headline, about, email)
                VALUES ('Erickson', 'Titular', 'Presentación', 'contacto@example.com')
                """);
        send("192.0.2.1", "Ana Pérez", "ana@example.com", "Hola, ¿hablamos?").andExpect(status().isAccepted());
        send("192.0.2.1", "Luis", "luis@example.com", "Segundo mensaje").andExpect(status().isAccepted());

        verify(notifier, times(2)).notifyNewMessage(any(ContactMessage.class), eq("contacto@example.com"));
        String inbox = mockMvc.perform(get("/api/admin/messages").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Luis", "Ana Pérez"))) // newest first
                .andExpect(jsonPath("$[1].message").value("Hola, ¿hablamos?"))
                .andExpect(jsonPath("$[1].read").value(false))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(inbox, "$[1].id");

        mockMvc.perform(put("/api/admin/messages/" + id + "/read").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").exists());
        mockMvc.perform(delete("/api/admin/messages/" + id).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/admin/messages/" + id).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/messages").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldKeepTheInboxForTheAdministrator() throws Exception {
        mockMvc.perform(get("/api/admin/messages")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/messages")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwt.token(
                                jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLimitTheMessagesOfEachIpAndDiscardTheBots() throws Exception {
        for (int i = 1; i <= 3; i++) {
            send("192.0.2.2", "Ana", "ana@example.com", "Mensaje " + i).andExpect(status().isAccepted());
        }
        send("192.0.2.2", "Ana", "ana@example.com", "Mensaje 4").andExpect(status().isTooManyRequests());
        // Another visitor is not affected.
        send("192.0.2.3", "Luis", "luis@example.com", "Hola").andExpect(status().isAccepted());

        mockMvc.perform(post("/api/public/contact")
                        .header("X-Real-IP", "192.0.2.4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Bot", "email": "bot@example.com", "message": "Oferta", "website": "x" }
                                """))
                .andExpect(status().isAccepted());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM contact_messages", Long.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM contact_messages WHERE name = 'Bot'", Long.class)).isZero();
    }

    private ResultActions send(String ip, String name, String email, String message) throws Exception {
        return mockMvc.perform(post("/api/public/contact")
                .header("X-Real-IP", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "%s", "email": "%s", "message": "%s" }
                        """.formatted(name, email, message)));
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
