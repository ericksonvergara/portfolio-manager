package com.portafolio_manager.backend.domain.technology;

/**
 * Another technology already has that name, ignoring case (HU021).
 */
public class DuplicateTechnologyNameException extends RuntimeException {

    public DuplicateTechnologyNameException(String name) {
        super("A technology named '" + name + "' already exists");
    }
}
