package com.portafolio_manager.backend.domain.project;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Builds the readable identifier of the public URL from the project name:
 * "Gestión de Inventario 2.0" becomes "gestion-de-inventario-2-0".
 */
public final class ProjectSlug {

    private ProjectSlug() {
    }

    public static String from(String name) {
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            throw new InvalidProjectException("The project name must contain letters or digits");
        }
        return slug;
    }
}
