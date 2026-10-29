package com.portafolio_manager.backend.application.service.contact;

import com.portafolio_manager.backend.application.port.out.contact.ContactMessageRepositoryPort;
import com.portafolio_manager.backend.application.port.out.contact.ContactNotifierPort;
import com.portafolio_manager.backend.application.port.out.contact.ContactRateLimiterPort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactMessageDraft;
import com.portafolio_manager.backend.domain.contact.ContactMessageNotFoundException;
import com.portafolio_manager.backend.domain.contact.ContactSubmission;
import com.portafolio_manager.backend.domain.contact.TooManyContactMessagesException;
import com.portafolio_manager.backend.domain.profile.Profile;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The contact form and its inbox (HU006).
 */
class ContactServicesTest {

    private static final Instant NOW = Instant.parse("2026-10-29T15:30:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final ContactMessageDraft DRAFT = new ContactMessageDraft("Ana", "ana@example.com", "Hola");
    private static final ContactSubmission SUBMISSION = new ContactSubmission(DRAFT, "203.0.113.7");
    private static final ContactMessage SAVED = new ContactMessage(12L, "Ana", "ana@example.com", "Hola", NOW, null);

    private final ContactMessageRepositoryPort repository = mock(ContactMessageRepositoryPort.class);
    private final ContactRateLimiterPort rateLimiter = mock(ContactRateLimiterPort.class);
    private final ProfileRepositoryPort profileRepository = mock(ProfileRepositoryPort.class);
    private final ContactNotifierPort notifier = mock(ContactNotifierPort.class);

    private final SendContactMessageService send =
            new SendContactMessageService(repository, rateLimiter, profileRepository, notifier, CLOCK);

    @Test
    void shouldSaveTheMessageAndNotifyTheContactEmailOfTheProfile() {
        when(rateLimiter.tryAcquire("203.0.113.7")).thenReturn(true);
        when(repository.save(DRAFT, NOW)).thenReturn(SAVED);
        when(profileRepository.find()).thenReturn(Optional.of(new Profile(
                "Erickson", "Titular", "Presentación", null, "contacto@example.com", null, List.of())));

        assertThat(send.execute(SUBMISSION)).isEqualTo(SAVED);
        verify(notifier).notifyNewMessage(SAVED, "contacto@example.com");
    }

    @Test
    void shouldKeepTheMessageInTheInboxWhenThereIsNoProfileToNotify() {
        when(rateLimiter.tryAcquire("203.0.113.7")).thenReturn(true);
        when(repository.save(DRAFT, NOW)).thenReturn(SAVED);
        when(profileRepository.find()).thenReturn(Optional.empty());

        assertThat(send.execute(SUBMISSION)).isEqualTo(SAVED);
        verifyNoInteractions(notifier);
    }

    @Test
    void shouldRejectASenderOverTheLimitWithoutSavingAnything() {
        when(rateLimiter.tryAcquire("203.0.113.7")).thenReturn(false);

        assertThatThrownBy(() -> send.execute(SUBMISSION)).isInstanceOf(TooManyContactMessagesException.class);
        verifyNoInteractions(repository, notifier);
    }

    @Test
    void shouldListMarkAndDeleteTheMessages() {
        when(repository.findAll()).thenReturn(List.of(SAVED));
        assertThat(new ListContactMessagesService(repository).execute(null)).containsExactly(SAVED);

        ContactMessage read = new ContactMessage(12L, "Ana", "ana@example.com", "Hola", NOW, NOW);
        when(repository.markRead(12L, NOW)).thenReturn(Optional.of(read));
        assertThat(new MarkContactMessageReadService(repository, CLOCK).execute(12L).read()).isTrue();

        when(repository.delete(12L)).thenReturn(true);
        new DeleteContactMessageService(repository).execute(12L);
        verify(repository).delete(12L);
    }

    @Test
    void shouldReportAnUnknownMessage() {
        when(repository.markRead(any(Long.class), any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new MarkContactMessageReadService(repository, CLOCK).execute(99L))
                .isInstanceOf(ContactMessageNotFoundException.class)
                .hasMessage("No contact message with id 99");

        when(repository.delete(99L)).thenReturn(false);
        assertThatThrownBy(() -> new DeleteContactMessageService(repository).execute(99L))
                .isInstanceOf(ContactMessageNotFoundException.class);
    }
}
