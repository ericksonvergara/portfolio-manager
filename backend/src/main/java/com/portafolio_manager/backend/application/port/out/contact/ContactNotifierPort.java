package com.portafolio_manager.backend.application.port.out.contact;

import com.portafolio_manager.backend.domain.contact.ContactMessage;

/**
 * Tells the portfolio owner that a message arrived. A failure is logged, never thrown: the message is
 * already saved and the administrator can read it in the inbox.
 */
public interface ContactNotifierPort {

    void notifyNewMessage(ContactMessage message, String recipient);
}
