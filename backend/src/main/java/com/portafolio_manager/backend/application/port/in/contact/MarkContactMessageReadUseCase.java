package com.portafolio_manager.backend.application.port.in.contact;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.contact.ContactMessage;

/**
 * Marks a message as read. Throws {@code ContactMessageNotFoundException}.
 */
public interface MarkContactMessageReadUseCase extends UseCase<Long, ContactMessage> {
}
