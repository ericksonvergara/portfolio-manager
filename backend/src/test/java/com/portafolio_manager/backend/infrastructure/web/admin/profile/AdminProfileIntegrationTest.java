package com.portafolio_manager.backend.infrastructure.web.admin.profile;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import com.portafolio_manager.backend.infrastructure.storage.TestImages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Profile administration (HU023) against the real schema and the file system.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class AdminProfileIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM profiles");
    }

    @Test
    void shouldCreateTheProfileTheFirstTimeAndUpdateItAfterwards() throws Exception {
        // Production has no demo seed: the first save creates the profile.
        mockMvc.perform(get("/api/admin/profile").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNotFound());
        save("Erickson Vergara", "contacto@example.com", """
                { "platform": "GITHUB", "url": "https://github.com/ericksonvergara" },
                { "platform": "LINKEDIN", "url": "https://www.linkedin.com/in/erickson" }
                """).andExpect(status().isOk());

        // Renamed, with a new email, the links reordered, one removed and one added.
        save("Erickson Vergara Rojas", "nuevo@example.com", """
                { "platform": "WEBSITE", "url": "https://erickson.dev" },
                { "platform": "GITHUB", "url": "https://github.com/erickson" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara Rojas"))
                .andExpect(jsonPath("$.email").value("nuevo@example.com"));

        mockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara Rojas"))
                .andExpect(jsonPath("$.location").doesNotExist())
                .andExpect(jsonPath("$.socialLinks[*].platform").value(contains("WEBSITE", "GITHUB")))
                .andExpect(jsonPath("$.socialLinks[1].url").value("https://github.com/erickson"));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM profiles", Long.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM social_links", Long.class)).isEqualTo(2);
    }

    @Test
    void shouldRejectAnInvalidEmailWithoutChangingTheProfile() throws Exception {
        save("Erickson Vergara", "contacto@example.com", "").andExpect(status().isOk());

        save("Otro nombre", "contacto@example", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email must be a valid email address"));
        save("Otro nombre", "contacto@example.com", """
                { "platform": "GITHUB", "url": "https://github.com/uno" },
                { "platform": "GITHUB", "url": "https://github.com/dos" }
                """).andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/public/profile")).andExpect(jsonPath("$.fullName").value("Erickson Vergara"));
    }

    @Test
    void shouldKeepThePhotoWhenSavingAndReplaceOrRemoveItsFile() throws Exception {
        uploadPhoto().andExpect(status().isNotFound()); // save the profile first
        save("Erickson Vergara", "contacto@example.com", "").andExpect(status().isOk());

        String first = photoUrl(uploadPhoto().andExpect(status().isOk()));
        assertThat(stored(first)).exists();
        save("Erickson Vergara Rojas", "contacto@example.com", "")
                .andExpect(jsonPath("$.photoUrl").value(first));

        String second = photoUrl(uploadPhoto().andExpect(status().isOk()));
        assertThat(stored(second)).exists();
        assertThat(stored(first)).doesNotExist();
        mockMvc.perform(get("/api/public/profile")).andExpect(jsonPath("$.photoUrl").value(second));

        mockMvc.perform(delete("/api/admin/profile/photo").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").doesNotExist());
        assertThat(stored(second)).doesNotExist();
    }

    private ResultActions save(String fullName, String email, String links) throws Exception {
        return mockMvc.perform(put("/api/admin/profile")
                .header(HttpHeaders.AUTHORIZATION, admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "fullName": "%s",
                          "headline": "Desarrollador Full Stack",
                          "about": "Presentación.",
                          "email": "%s",
                          "location": "",
                          "socialLinks": [%s]
                        }
                        """.formatted(fullName, email, links)));
    }

    private ResultActions uploadPhoto() throws Exception {
        return mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/profile/photo")
                .file(new MockMultipartFile("file", "foto.png", "image/png", TestImages.PNG))
                .header(HttpHeaders.AUTHORIZATION, admin()));
    }

    private static String photoUrl(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.photoUrl");
    }

    private static Path stored(String url) {
        return TestImages.UPLOADS_DIR.resolve(url.substring("/uploads/".length()));
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
