package com.portafolio_manager.backend.infrastructure.mail;

import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.infrastructure.contact.ContactProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpContactNotifierTest {

    private static final ContactMessage MESSAGE = new ContactMessage(
            12L, "Ana Pérez", "ana@example.com", "Hola, ¿hablamos?", Instant.parse("2026-10-29T15:30:00Z"), null);

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpContactNotifier notifier = new SmtpContactNotifier(
            mailSender, new ContactProperties(5, Duration.ofHours(1), "Portfolio <no-reply@portfolio.test>"));

    @Test
    void shouldEmailTheOwnerSoThatReplyAnswersTheVisitor() {
        notifier.notifyNewMessage(MESSAGE, "contacto@example.com");

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(sent.capture());
        assertThat(sent.getValue().getTo()).containsExactly("contacto@example.com");
        assertThat(sent.getValue().getReplyTo()).isEqualTo("ana@example.com");
        assertThat(sent.getValue().getFrom()).isEqualTo("Portfolio <no-reply@portfolio.test>");
        assertThat(sent.getValue().getSubject()).isEqualTo("Nuevo mensaje de contacto de Ana Pérez");
        assertThat(sent.getValue().getText()).contains("Hola, ¿hablamos?", "ana@example.com");
    }

    @Test
    void shouldNotFailWhenTheMailServerIsDown() {
        doThrow(new MailSendException("connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> notifier.notifyNewMessage(MESSAGE, "contacto@example.com")).doesNotThrowAnyException();
    }
}
