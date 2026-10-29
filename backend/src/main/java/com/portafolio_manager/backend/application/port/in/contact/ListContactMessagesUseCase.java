package com.portafolio_manager.backend.application.port.in.contact;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.contact.ContactMessage;

import java.util.List;

/**
 * Takes no input; returns the inbox, the newest message first (HU006).
 */
public interface ListContactMessagesUseCase extends UseCase<Void, List<ContactMessage>> {
}
