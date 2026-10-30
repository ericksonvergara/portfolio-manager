package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * How a visitor narrows the portfolio. The text (HU004) matches the name and the technologies of a project,
 * ignoring case and accents; with several words, every one must match. The optional category (HU005) keeps
 * the projects of that kind. A blank text without category matches everything.
 */
public record ProjectSearch(String text, ProjectCategory category) {

    public static final int MAX_LENGTH = 100;

    private static final Pattern MARKS = Pattern.compile("\\p{M}");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    public ProjectSearch {
        text = text == null ? "" : text.strip();
        if (text.length() > MAX_LENGTH) {
            throw new InvalidProjectException("search must be at most " + MAX_LENGTH + " characters");
        }
    }

    public ProjectSearch(String text) {
        this(text, null);
    }

    public static ProjectSearch all() {
        return new ProjectSearch(null, null);
    }

    public boolean matches(ProjectSummary project) {
        if (category != null && !category.includes(project)) {
            return false;
        }
        if (text.isEmpty()) {
            return true;
        }
        String name = normalize(project.name());
        List<String> technologies = project.technologies().stream()
                .map(Technology::name)
                .map(ProjectSearch::normalize)
                .toList();
        return Arrays.stream(SPACES.split(normalize(text)))
                .allMatch(term -> name.contains(term) || technologies.stream().anyMatch(t -> t.contains(term)));
    }

    /** "Gestión de Imágenes" → "gestion de imagenes". */
    private static String normalize(String value) {
        return MARKS.matcher(Normalizer.normalize(value, Normalizer.Form.NFD)).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
