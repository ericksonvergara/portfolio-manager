package com.portafolio_manager.backend.application.port.in.contact;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactSubmission;

/**
 * Saves a message from the contact form and notifies the portfolio owner (HU006). Throws
 * {@code TooManyContactMessagesException} when the sender is over the limit.
 */
public interface SendContactMessageUseCase extends UseCase<ContactSubmission, ContactMessage> {
}
