package com.vibefolio.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Centralized exception → HTTP response mapping.
 * All errors return RFC 7807 {@link ProblemDetail} for consistent FE error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation failures (e.g. @Valid on request DTOs).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://vibefolio.com/errors/validation"));
        problem.setTitle("Validation Failed");
        problem.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("invalid request"));
        return problem;
    }

    /**
     * Unsupported operation — stub endpoints not yet implemented.
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    ProblemDetail handleNotImplemented(UnsupportedOperationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_IMPLEMENTED);
        problem.setType(URI.create("https://vibefolio.com/errors/not-implemented"));
        problem.setTitle("Not Implemented");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Catch-all for unexpected errors.
     * Detail is intentionally vague to avoid leaking internals.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://vibefolio.com/errors/internal"));
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please try again.");
        return problem;
    }
}
