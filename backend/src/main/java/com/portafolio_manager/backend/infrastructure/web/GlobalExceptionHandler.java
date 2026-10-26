package com.portafolio_manager.backend.infrastructure.web;

import com.portafolio_manager.backend.domain.auth.InvalidCredentialsException;
import com.portafolio_manager.backend.domain.auth.InvalidPasswordResetTokenException;
import com.portafolio_manager.backend.domain.auth.InvalidRefreshTokenException;
import com.portafolio_manager.backend.domain.image.ImageTooLargeException;
import com.portafolio_manager.backend.domain.image.InvalidImageException;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.InvalidProjectException;
import com.portafolio_manager.backend.domain.project.ProjectImageNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.InvalidTechnologyException;
import com.portafolio_manager.backend.domain.technology.TechnologyInUseException;
import com.portafolio_manager.backend.domain.technology.TechnologyNotFoundException;
import com.portafolio_manager.backend.domain.user.IncorrectPasswordException;
import com.portafolio_manager.backend.domain.user.UnchangedPasswordException;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    ResponseEntity<ApiErrorResponse> handleAuthenticationFailure(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler({
            ProfileNotFoundException.class,
            ProjectNotFoundException.class,
            ProjectImageNotFoundException.class,
            TechnologyNotFoundException.class
    })
    ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ImageTooLargeException.class)
    ResponseEntity<ApiErrorResponse> handleTooLarge(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.CONTENT_TOO_LARGE, exception.getMessage(), request);
    }

    /** The multipart limit (UPLOAD_MAX_SIZE) is checked before the request reaches a controller. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> handleUploadTooLarge(HttpServletRequest request) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "The file exceeds the maximum upload size", request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<ApiErrorResponse> handleMissingFile(MissingServletRequestPartException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Missing file part '" + exception.getRequestPartName() + "'", request);
    }

    @ExceptionHandler({
            InvalidProjectException.class,
            InvalidImageException.class,
            InvalidTechnologyException.class,
            WeakPasswordException.class,
            InvalidPasswordResetTokenException.class,
            // 400, not 401: the frontend treats a 401 as an expired session.
            IncorrectPasswordException.class,
            UnchangedPasswordException.class
    })
    ResponseEntity<ApiErrorResponse> handleInvalidData(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler({
            DuplicateProjectNameException.class,
            ProjectNotPublishableException.class,
            DuplicateTechnologyNameException.class,
            TechnologyInUseException.class
    })
    ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    /**
     * A constraint of the database rejected the change: another request changed the same data at the same
     * time (for example, a technology deleted while a project was being saved with it). The SQL details stay
     * in the server log.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleIntegrityViolation(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "The data changed while saving. Reload it and try again", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiErrorResponse.of(status, message, request.getRequestURI()));
    }
}
