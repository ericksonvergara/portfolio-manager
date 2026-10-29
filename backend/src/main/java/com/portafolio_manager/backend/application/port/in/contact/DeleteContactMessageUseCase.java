package com.portafolio_manager.backend.application.port.in.contact;

import com.portafolio_manager.backend.application.port.in.UseCase;

/**
 * Deletes a message from the inbox. Throws {@code ContactMessageNotFoundException}.
 */
public interface DeleteContactMessageUseCase extends UseCase<Long, Void> {
}
