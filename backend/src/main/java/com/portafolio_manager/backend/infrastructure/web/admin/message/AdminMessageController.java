package com.portafolio_manager.backend.infrastructure.web.admin.message;

import com.portafolio_manager.backend.application.port.in.contact.DeleteContactMessageUseCase;
import com.portafolio_manager.backend.application.port.in.contact.ListContactMessagesUseCase;
import com.portafolio_manager.backend.application.port.in.contact.MarkContactMessageReadUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/messages")
class AdminMessageController implements AdminMessageApi {

    private final ListContactMessagesUseCase listContactMessagesUseCase;
    private final MarkContactMessageReadUseCase markContactMessageReadUseCase;
    private final DeleteContactMessageUseCase deleteContactMessageUseCase;

    AdminMessageController(
            ListContactMessagesUseCase listContactMessagesUseCase,
            MarkContactMessageReadUseCase markContactMessageReadUseCase,
            DeleteContactMessageUseCase deleteContactMessageUseCase
    ) {
        this.listContactMessagesUseCase = listContactMessagesUseCase;
        this.markContactMessageReadUseCase = markContactMessageReadUseCase;
        this.deleteContactMessageUseCase = deleteContactMessageUseCase;
    }

    @Override
    @GetMapping
    public List<ContactMessageResponse> listMessages() {
        return listContactMessagesUseCase.execute(null).stream().map(ContactMessageResponse::from).toList();
    }

    @Override
    @PutMapping("/{id}/read")
    public ContactMessageResponse markRead(@PathVariable Long id) {
        return ContactMessageResponse.from(markContactMessageReadUseCase.execute(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        deleteContactMessageUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
