package com.portafolio_manager.backend.domain.project;

/**
 * Another project already has that name, or an equivalent one: names that differ only in case, accents
 * or punctuation produce the same slug, and so the same public URL (RN001).
 */
public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String name) {
        super("A project named '" + name + "', or with an equivalent name, already exists");
    }
}
