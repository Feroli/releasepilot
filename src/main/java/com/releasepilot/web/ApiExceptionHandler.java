package com.releasepilot.web;

import com.releasepilot.promotion.domain.DomainErrorCode;
import com.releasepilot.promotion.domain.DomainException;
import java.net.URI;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
        HttpStatus status = statusFor(ex.code());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create("https://github.com/Feroli/releasepilot/errors/" + ex.code().name().toLowerCase()));
        problem.setTitle(ex.code().name());
        problem.setProperty("code", ex.code().name());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("VALIDATION_FAILED");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is invalid");
        problem.setTitle("INVALID_REQUEST_BODY");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleConflict(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request conflicts with current state");
        problem.setTitle("CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    private HttpStatus statusFor(DomainErrorCode code) {
        return switch (code) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case APPROVER_REQUIRED -> HttpStatus.FORBIDDEN;
            case RESOURCE_ALREADY_EXISTS,
                    ENVIRONMENT_SKIPPED,
                    PREVIOUS_ENVIRONMENT_INCOMPLETE,
                    PROMOTION_ALREADY_IN_PROGRESS,
                    INVALID_PROMOTION_STATE,
                    TARGET_ENVIRONMENT_ALREADY_COMPLETED,
                    PROMOTION_IMMUTABLE -> HttpStatus.CONFLICT;
            case INVALID_ENVIRONMENT, INVALID_PAGINATION -> HttpStatus.BAD_REQUEST;
        };
    }
}

