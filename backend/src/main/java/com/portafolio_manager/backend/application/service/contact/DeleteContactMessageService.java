package com.portafolio_manager.backend.application.service.contact;

import com.portafolio_manager.backend.application.port.in.contact.DeleteContactMessageUseCase;
import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessageNotFoundException;

public class DeleteContactMessageService implements DeleteContactMessageUseCase {

    private final ContactMessageRepositoryPort messageRepository;

    public DeleteContactMessageService(ContactMessageRepositoryPort messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Void execute(Long id) {
        if (!messageRepository.delete(id)) {
            throw new ContactMessageNotFoundException(id);
        }
        return null;
    }
}
