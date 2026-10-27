package com.portafolio_manager.backend.domain.technology;

public class TechnologyNotFoundException extends RuntimeException {

    public TechnologyNotFoundException(Long id) {
        super("No technology with id " + id);
    }
}
