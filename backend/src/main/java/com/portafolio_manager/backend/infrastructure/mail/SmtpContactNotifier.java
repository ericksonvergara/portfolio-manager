package com.portafolio_manager.backend.infrastructure.mail;

import com.portafolio_manager.backend.application.port.out.contact.ContactNotifierPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.infrastructure.contact.ContactProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Emails the portfolio owner when a contact message arrives (HU006). "Reply" answers the visitor
 * directly. Locally and in CI the server is Mailpit.
 */
@Component
class SmtpContactNotifier implements ContactNotifierPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpContactNotifier.class);

    private final JavaMailSender mailSender;
    private final ContactProperties properties;

    SmtpContactNotifier(JavaMailSender mailSender, ContactProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void notifyNewMessage(ContactMessage contact, String recipient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mailFrom());
        message.setTo(recipient);
        message.setReplyTo(contact.email());
        message.setSubject("Nuevo mensaje de contacto de " + contact.name());
        message.setText("""
                %s (%s) te escribió desde el formulario de contacto del portafolio:

                %s

                Responde a este correo para contestarle, o léelo en el panel de administración, en Mensajes.
                """.formatted(contact.name(), contact.email(), contact.message()));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // The message is already saved: the administrator still finds it in the inbox.
            log.error("Could not send the contact message notice", exception);
        }
    }
}
