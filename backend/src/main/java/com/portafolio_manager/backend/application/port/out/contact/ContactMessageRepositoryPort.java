package com.portafolio_manager.backend.application.port.out.contact;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactMessageDraft;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContactMessageRepositoryPort extends PersistencePort {

    ContactMessage save(ContactMessageDraft draft, Instant receivedAt);

    /** Every message, the newest first. */
    List<ContactMessage> findAll();

    /** Marks the message as read (keeping the first reading time); empty if it does not exist. */
    Optional<ContactMessage> markRead(long id, Instant readAt);

    /** Returns false if the message does not exist. */
    boolean delete(long id);
}
