package com.portafolio_manager.backend.infrastructure.web.contact;

import com.portafolio_manager.backend.application.port.in.contact.SendContactMessageUseCase;
import com.portafolio_manager.backend.domain.contact.ContactSubmission;
import com.portafolio_manager.backend.infrastructure.web.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/contact")
class ContactController implements ContactApi {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final SendContactMessageUseCase sendContactMessageUseCase;

    ContactController(SendContactMessageUseCase sendContactMessageUseCase) {
        this.sendContactMessageUseCase = sendContactMessageUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> sendMessage(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest servletRequest
    ) {
        if (request.isSpam()) {
            // Same answer as a real message, so the bot learns nothing.
            log.info("Contact message discarded: the hidden field was filled in");
            return ResponseEntity.accepted().build();
        }
        sendContactMessageUseCase.execute(new ContactSubmission(request.toDraft(), ClientIp.of(servletRequest)));
        return ResponseEntity.accepted().build();
    }
}
