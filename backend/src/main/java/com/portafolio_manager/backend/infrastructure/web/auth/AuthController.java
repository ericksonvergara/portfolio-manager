package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.in.auth.LoginCommand;
import com.portafolio_manager.backend.application.port.in.auth.LoginUseCase;
import com.portafolio_manager.backend.application.port.in.auth.LogoutCommand;
import com.portafolio_manager.backend.application.port.in.auth.LogoutUseCase;
import com.portafolio_manager.backend.application.port.in.auth.RefreshSessionUseCase;
import com.portafolio_manager.backend.application.port.in.auth.RequestPasswordResetUseCase;
import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

@RestController
@RequestMapping("/api/auth")
class AuthController implements AuthApi {

    private static final String BEARER_PREFIX = "Bearer ";

    private final LoginUseCase loginUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenCookieFactory cookieFactory;

    AuthController(
            LoginUseCase loginUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            LogoutUseCase logoutUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            RefreshTokenCookieFactory cookieFactory
    ) {
        this.loginUseCase = loginUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.cookieFactory = cookieFactory;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return sessionResponse(result);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request) {
        AuthenticationResult result = refreshSessionUseCase.execute(readRefreshToken(request));
        return sessionResponse(result);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        logoutUseCase.execute(new LogoutCommand(readRefreshToken(request), readBearerToken(request)));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                .build();
    }

    @Override
    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        requestPasswordResetUseCase.execute(request.email());
        return ResponseEntity.accepted().build();
    }

    @Override
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        resetPasswordUseCase.execute(new ResetPasswordCommand(request.token(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<TokenResponse> sessionResponse(AuthenticationResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieFactory.create(result.refreshToken(), result.refreshTokenExpiresAt()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(TokenResponse.from(result));
    }

    private String readRefreshToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieFactory.cookieName());
        return cookie == null ? null : cookie.getValue();
    }

    private static String readBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
