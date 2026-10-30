package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import com.portafolio_manager.backend.infrastructure.storage.TestImages;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Image management (HU019) against the real schema and the file system. The upload limit is lowered
 * to 1 KB so the size check can be exercised with small files.
 */
@SpringBootTest(properties = {TestJwt.SECRET_PROPERTY, "app.uploads.max-size=1KB"})
@AutoConfigureMockMvc
class AdminProjectImageIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    private long projectId;

    @BeforeEach
    void createProject() {
        long java = jdbcTemplate.queryForObject(
                "INSERT INTO technologies (name, category, display_order) VALUES ('Java', 'BACKEND', 1) RETURNING id",
                Long.class);
        projectId = jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description)
                VALUES ('Sistema ERP', 'sistema-erp', 'Corta', 'Completa') RETURNING id
                """, Long.class);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?)", projectId, java);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldUploadACoverThatAllowsPublishingAndReplaceItsFile() throws Exception {
        String first = coverUrl(uploadCover(png("portada.png")).andExpect(status().isOk()));
        // HU020: the image keeps its quality, the stored file is the uploaded one, byte for byte.
        assertThat(Files.readAllBytes(stored(first))).isEqualTo(TestImages.PNG);

        // RN004 satisfied: the project can now be published and its cover is public.
        publish(true).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/projects/sistema-erp"))
                .andExpect(jsonPath("$.coverImageUrl").value(first));

        String second = coverUrl(uploadCover(png("nueva.png")).andExpect(status().isOk()));
        assertThat(second).isNotEqualTo(first);
        assertThat(stored(second)).exists();
        assertThat(stored(first)).doesNotExist();

        // An active project keeps its cover (RN004)...
        mockMvc.perform(delete("/api/admin/projects/" + projectId + "/cover").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isConflict());
        // ...until it is deactivated.
        publish(false).andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/projects/" + projectId + "/cover").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl").doesNotExist());
        assertThat(stored(second)).doesNotExist();
    }

    @Test
    void shouldNotPublishAProjectWithoutTechnologies() throws Exception {
        // HU030: possible only for data loaded outside the forms, which always require a technology.
        jdbcTemplate.update("DELETE FROM project_technologies WHERE project_id = ?", projectId);
        uploadCover(png("portada.png")).andExpect(status().isOk());

        publish(true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Project 'Sistema ERP' needs at least one technology before it can be published"));
        mockMvc.perform(get("/api/public/projects")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectFilesThatAreNotValidImages() throws Exception {
        uploadCover(new MockMultipartFile("file", "falsa.png", "image/png", "<script>".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The file content is not a JPG or PNG image"));
        uploadCover(new MockMultipartFile("file", "grande.png", "image/png", large()))
                .andExpect(status().isContentTooLarge());
        uploadCover(new MockMultipartFile("file", "foto.gif", "image/gif", TestImages.PNG))
                .andExpect(status().isBadRequest());

        Long covers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE cover_image_url IS NOT NULL", Long.class);
        assertThat(covers).isZero();
    }

    @Test
    void shouldManageTheGalleryAndDeleteTheFilesWithTheProject() throws Exception {
        String cover = coverUrl(uploadCover(png("portada.png")));
        addImage(png("uno.png"), "Pantalla principal")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gallery", hasSize(1)))
                .andExpect(jsonPath("$.gallery[0].altText").value("Pantalla principal"));
        String response = addImage(new MockMultipartFile("file", "dos.jpg", "image/jpeg", TestImages.JPEG), null)
                .andExpect(jsonPath("$.gallery", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        Integer firstId = JsonPath.read(response, "$.gallery[0].id");
        String firstUrl = JsonPath.read(response, "$.gallery[0].url");
        String secondUrl = JsonPath.read(response, "$.gallery[1].url");
        assertThat(secondUrl).endsWith(".jpg");

        mockMvc.perform(delete("/api/admin/projects/" + projectId + "/images/" + firstId)
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gallery", hasSize(1)));
        assertThat(stored(firstUrl)).doesNotExist();
        mockMvc.perform(delete("/api/admin/projects/" + projectId + "/images/" + firstId)
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/admin/projects/" + projectId).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNoContent());
        assertThat(stored(cover)).doesNotExist();
        assertThat(stored(secondUrl)).doesNotExist();
    }

    private ResultActions uploadCover(MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/projects/" + projectId + "/cover")
                .file(file).header(HttpHeaders.AUTHORIZATION, admin()));
    }

    private ResultActions addImage(MockMultipartFile file, String altText) throws Exception {
        var request = multipart("/api/admin/projects/" + projectId + "/images").file(file)
                .header(HttpHeaders.AUTHORIZATION, admin());
        if (altText != null) {
            request.param("altText", altText);
        }
        return mockMvc.perform(request);
    }

    private ResultActions publish(boolean active) throws Exception {
        return mockMvc.perform(patch("/api/admin/projects/" + projectId + "/publication")
                .header(HttpHeaders.AUTHORIZATION, admin())
                .contentType("application/json")
                .content("{\"active\": " + active + "}"));
    }

    private static String coverUrl(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.coverImageUrl");
    }

    private static Path stored(String url) {
        return TestImages.UPLOADS_DIR.resolve(url.substring("/uploads/".length()));
    }

    private static MockMultipartFile png(String name) {
        return new MockMultipartFile("file", name, "image/png", TestImages.PNG);
    }

    private static byte[] large() {
        byte[] content = new byte[2048];
        System.arraycopy(TestImages.PNG, 0, content, 0, TestImages.PNG.length);
        return content;
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
