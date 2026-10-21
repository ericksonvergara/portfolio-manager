package com.portafolio_manager.backend.domain.project;

/**
 * Another project already has that name, or a name that produces the same slug (RN001).
 */
public class DuplicateProjectNameException extends RuntimeException {

    public DuplicateProjectNameException(String name) {
        super("A project named '" + name + "' already exists");
    }
}
