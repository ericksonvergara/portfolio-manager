package com.portafolio_manager.backend.domain.technology;

/**
 * A technology used by projects cannot be deleted: they could be left without any (RN005).
 */
public class TechnologyInUseException extends RuntimeException {

    public TechnologyInUseException(String name, long projectCount) {
        super("Technology '" + name + "' is used by " + projectCount + " project(s): remove it from them first");
    }
}
