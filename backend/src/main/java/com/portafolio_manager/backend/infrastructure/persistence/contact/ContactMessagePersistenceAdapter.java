package com.portafolio_manager.backend.infrastructure.persistence.contact;

import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactMessageDraft;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
class ContactMessagePersistenceAdapter implements ContactMessageRepositoryPort {

    private final ContactMessageJpaRepository messageRepository;

    ContactMessagePersistenceAdapter(ContactMessageJpaRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public ContactMessage save(ContactMessageDraft draft, Instant receivedAt) {
        return toDomain(messageRepository.saveAndFlush(ContactMessageJpaEntity.newMessage(draft, receivedAt)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessage> findAll() {
        return messageRepository.findAllByOrderByReceivedAtDescIdDesc().stream()
                .map(ContactMessagePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Optional<ContactMessage> markRead(long id, Instant readAt) {
        return messageRepository.findById(id).map(entity -> {
            entity.markRead(readAt);
            return toDomain(messageRepository.saveAndFlush(entity));
        });
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        if (!messageRepository.existsById(id)) {
            return false;
        }
        messageRepository.deleteById(id);
        return true;
    }

    private static ContactMessage toDomain(ContactMessageJpaEntity entity) {
        return new ContactMessage(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getMessage(),
                entity.getReceivedAt(),
                entity.getReadAt()
        );
    }
}
