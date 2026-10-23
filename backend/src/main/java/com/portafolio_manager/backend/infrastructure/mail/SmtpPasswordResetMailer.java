package com.portafolio_manager.backend.infrastructure.mail;

import com.portafolio_manager.backend.application.port.out.auth.PasswordResetMailerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Sends the recovery email over SMTP (MAIL_HOST, MAIL_PORT…). Locally and in CI the server is
 * Mailpit, which keeps the messages instead of delivering them.
 */
@Component
@EnableConfigurationProperties(PasswordResetProperties.class)
class SmtpPasswordResetMailer implements PasswordResetMailerPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpPasswordResetMailer.class);

    private final JavaMailSender mailSender;
    private final PasswordResetProperties properties;

    SmtpPasswordResetMailer(JavaMailSender mailSender, PasswordResetProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendPasswordReset(String email, String name, String resetLink, Duration validity) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mailFrom());
        message.setTo(email);
        message.setSubject("Recupera tu contraseña de Portfolio Manager");
        message.setText("""
                Hola, %s:

                Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                Abre este enlace para elegir una nueva (vale durante %d minutos y solo una vez):

                %s

                Si no fuiste tú, ignora este correo: tu contraseña no cambiará.
                """.formatted(name, validity.toMinutes(), resetLink));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // The request answers the same either way, so a failure must not reveal that the email exists.
            log.error("Could not send the password reset email", exception);
        }
    }
}
