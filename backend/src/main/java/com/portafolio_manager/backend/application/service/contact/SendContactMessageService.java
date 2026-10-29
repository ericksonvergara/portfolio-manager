package com.portafolio_manager.backend.application.service.contact;

import com.portafolio_manager.backend.application.port.in.contact.SendContactMessageUseCase;
import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.application.port.out.contact.ContactNotifierPort;
import com.portafolio_manager.backend.application.port.out.contact.ContactRateLimiterPort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactSubmission;
import com.portafolio_manager.backend.domain.contact.TooManyContactMessagesException;

import java.time.Clock;

public class SendContactMessageService implements SendContactMessageUseCase {

    private final ContactMessageRepositoryPort messageRepository;
    private final ContactRateLimiterPort rateLimiter;
    private final ProfileRepositoryPort profileRepository;
    private final ContactNotifierPort notifier;
    private final Clock clock;

    public SendContactMessageService(
            ContactMessageRepositoryPort messageRepository,
            ContactRateLimiterPort rateLimiter,
            ProfileRepositoryPort profileRepository,
            ContactNotifierPort notifier,
            Clock clock
    ) {
        this.messageRepository = messageRepository;
        this.rateLimiter = rateLimiter;
        this.profileRepository = profileRepository;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public ContactMessage execute(ContactSubmission submission) {
        if (!rateLimiter.tryAcquire(submission.senderKey())) {
            throw new TooManyContactMessagesException();
        }

        ContactMessage saved = messageRepository.save(submission.draft(), clock.instant());
        // The notice goes to the public contact email; without a profile the message still waits in the inbox.
        profileRepository.find().ifPresent(profile -> notifier.notifyNewMessage(saved, profile.email()));
        return saved;
    }
}
