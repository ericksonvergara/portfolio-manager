package com.portafolio_manager.backend.infrastructure.persistence.contact;

import com.portafolio_manager.backend.domain.contact.ContactMessageDraft;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "contact_messages")
public class ContactMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected ContactMessageJpaEntity() {
    }

    static ContactMessageJpaEntity newMessage(ContactMessageDraft draft, Instant receivedAt) {
        ContactMessageJpaEntity entity = new ContactMessageJpaEntity();
        entity.name = draft.name();
        entity.email = draft.email();
        entity.message = draft.message();
        entity.receivedAt = receivedAt;
        return entity;
    }

    /** Keeps the first reading time. */
    void markRead(Instant at) {
        if (readAt == null) {
            readAt = at;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
