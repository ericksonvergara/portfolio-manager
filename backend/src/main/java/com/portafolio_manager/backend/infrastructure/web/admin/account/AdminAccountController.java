package com.portafolio_manager.backend.infrastructure.web.admin.account;

import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/account")
class AdminAccountController implements AdminAccountApi {

    private final ChangePasswordUseCase changePasswordUseCase;

    AdminAccountController(ChangePasswordUseCase changePasswordUseCase) {
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @Override
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        changePasswordUseCase.execute(new ChangePasswordCommand(
                Long.valueOf(jwt.getSubject()),
                request.currentPassword(),
                request.newPassword(),
                jwt.getId(),
                jwt.getExpiresAt()
        ));
        return ResponseEntity.noContent().build();
    }
}
