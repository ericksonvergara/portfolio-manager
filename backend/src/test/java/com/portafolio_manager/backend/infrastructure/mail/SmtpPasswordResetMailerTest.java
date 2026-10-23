package com.portafolio_manager.backend.infrastructure.mail;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpPasswordResetMailerTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpPasswordResetMailer mailer = new SmtpPasswordResetMailer(mailSender,
            new PasswordResetProperties(Duration.ofMinutes(30), "http://localhost/reset", "no-reply@portfolio.local"));

    @Test
    void shouldSendTheLinkWithItsValidity() {
        mailer.sendPasswordReset("admin@example.com", "Erickson", "http://localhost/reset?token=abc", Duration.ofMinutes(30));

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(sent.capture());
        SimpleMailMessage message = sent.getValue();
        assertThat(message.getTo()).containsExactly("admin@example.com");
        assertThat(message.getFrom()).isEqualTo("no-reply@portfolio.local");
        assertThat(message.getSubject()).contains("Recupera tu contraseña");
        assertThat(message.getText())
                .contains("Hola, Erickson")
                .contains("http://localhost/reset?token=abc")
                .contains("30 minutos");
    }

    @Test
    void shouldNotFailTheRequestWhenTheMailServerIsDown() {
        doThrow(new MailSendException("connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> mailer.sendPasswordReset("admin@example.com", "Erickson", "http://x", Duration.ofMinutes(30)))
                .doesNotThrowAnyException();
    }
}
