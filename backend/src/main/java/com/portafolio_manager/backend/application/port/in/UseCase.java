package com.portafolio_manager.backend.application.port.in;

public interface UseCase<I, O> {

    O execute(I input);
}
