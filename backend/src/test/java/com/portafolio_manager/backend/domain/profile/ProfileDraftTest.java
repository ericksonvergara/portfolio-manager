package com.portafolio_manager.backend.domain.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileDraftTest {

    private static final List<SocialLink> LINKS = List.of(
            new SocialLink(SocialPlatform.GITHUB, "https://github.com/ericksonvergara"),
            new SocialLink(SocialPlatform.LINKEDIN, "https://www.linkedin.com/in/erickson")
    );

    @Test
    void shouldTrimTheTextAndTreatABlankLocationAsNotSet() {
        ProfileDraft draft = new ProfileDraft(
                "  Erickson Vergara ", " Desarrollador ", " Presentación ", " contacto@example.com ", "  ", LINKS);

        assertThat(draft.fullName()).isEqualTo("Erickson Vergara");
        assertThat(draft.headline()).isEqualTo("Desarrollador");
        assertThat(draft.about()).isEqualTo("Presentación");
        assertThat(draft.email()).isEqualTo("contacto@example.com");
        assertThat(draft.location()).isNull();
        assertThat(draft.socialLinks()).containsExactlyElementsOf(LINKS);
    }

    @Test
    void shouldRequireTheNameTheHeadlineTheAboutAndTheEmail() {
        assertThatThrownBy(() -> draft(" ", "contacto@example.com"))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessage("fullName must not be blank");
        assertThatThrownBy(() -> new ProfileDraft("Erickson", "", "Presentación", "a@example.com", null, List.of()))
                .hasMessage("headline must not be blank");
        assertThatThrownBy(() -> new ProfileDraft("Erickson", "Titular", null, "a@example.com", null, List.of()))
                .hasMessage("about must not be blank");
        assertThatThrownBy(() -> draft("Erickson", null)).hasMessage("email must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"contacto", "contacto@", "@example.com", "contacto@example", "con tacto@example.com",
            "a@b@example.com", "contacto@example."})
    void shouldRejectAnInvalidEmail(String email) {
        assertThatThrownBy(() -> draft("Erickson", email))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessage("email must be a valid email address");
    }

    @ParameterizedTest
    @ValueSource(strings = {"contacto@example.com", "erickson.vergara+portafolio@mail.example.co"})
    void shouldAcceptAValidEmail(String email) {
        assertThat(draft("Erickson", email).email()).isEqualTo(email);
    }

    @Test
    void shouldLimitTheLengthOfTheText() {
        assertThatThrownBy(() -> draft("x".repeat(101), "contacto@example.com"))
                .hasMessage("fullName must be at most 100 characters");
        assertThatThrownBy(() -> new ProfileDraft(
                "Erickson", "Titular", "x".repeat(2001), "contacto@example.com", null, List.of()))
                .hasMessage("about must be at most 2000 characters");
        assertThatThrownBy(() -> new ProfileDraft(
                "Erickson", "Titular", "Presentación", "contacto@example.com", "x".repeat(101), List.of()))
                .hasMessage("location must be at most 100 characters");
    }

    @Test
    void shouldAcceptOneWebLinkPerPlatform() {
        List<SocialLink> repeated = List.of(
                new SocialLink(SocialPlatform.GITHUB, "https://github.com/uno"),
                new SocialLink(SocialPlatform.GITHUB, "https://github.com/dos"));
        assertThatThrownBy(() -> links(repeated))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessage("Only one link per platform is allowed: GITHUB");

        assertThatThrownBy(() -> links(List.of(new SocialLink(SocialPlatform.WEBSITE, "javascript:alert(1)"))))
                .hasMessage("WEBSITE link must be a valid http or https URL");
        assertThatThrownBy(() -> links(List.of(new SocialLink(SocialPlatform.X, "https://x.com/" + "a".repeat(500)))))
                .hasMessage("X link must be a valid http or https URL");
    }

    private static ProfileDraft draft(String fullName, String email) {
        return new ProfileDraft(fullName, "Titular", "Presentación", email, null, List.of());
    }

    private static ProfileDraft links(List<SocialLink> links) {
        return new ProfileDraft("Erickson", "Titular", "Presentación", "contacto@example.com", null, links);
    }
}
