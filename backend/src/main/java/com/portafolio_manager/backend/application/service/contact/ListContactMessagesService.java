package com.portafolio_manager.backend.application.service.contact;

import com.portafolio_manager.backend.application.port.in.contact.ListContactMessagesUseCase;
import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;

import java.util.List;

public class ListContactMessagesService implements ListContactMessagesUseCase {

    private final ContactMessageRepositoryPort messageRepository;

    public ListContactMessagesService(ContactMessageRepositoryPort messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<ContactMessage> execute(Void input) {
        return messageRepository.findAll();
    }
}
