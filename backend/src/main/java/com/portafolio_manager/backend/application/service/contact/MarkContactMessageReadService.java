package com.portafolio_manager.backend.application.service.contact;

import com.portafolio_manager.backend.application.port.in.contact.MarkContactMessageReadUseCase;
import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactMessageNotFoundException;

import java.time.Clock;

public class MarkContactMessageReadService implements MarkContactMessageReadUseCase {

    private final ContactMessageRepositoryPort messageRepository;
    private final Clock clock;

    public MarkContactMessageReadService(ContactMessageRepositoryPort messageRepository, Clock clock) {
        this.messageRepository = messageRepository;
        this.clock = clock;
    }

    @Override
    public ContactMessage execute(Long id) {
        return messageRepository.markRead(id, clock.instant())
                .orElseThrow(() -> new ContactMessageNotFoundException(id));
    }
}
