package com.jpmns.task.core.presentation.controller.common.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jpmns.task.core.application.port.security.exception.InvalidTokenException;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.domain.common.exception.DomainException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        LOGGER.error("Validation error: {}", ex.getMessage(), ex);

        var message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation error");

        return buildProblemDetail(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.error("Message not readable: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        LOGGER.error("Domain exception: {}", ex.getErrors(), ex);

        return buildProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, String.join(", ", ex.getErrors()));
    }

    @ExceptionHandler({UserNotFoundException.class, TaskNotFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        LOGGER.error("Resource not found: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleConflict(Exception ex) {
        LOGGER.error("Conflict error: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    public ProblemDetail handleUnauthorized(Exception ex) {
        LOGGER.error("Unauthorized access: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    public ProblemDetail handleForbidden(Exception ex) {
        LOGGER.error("Access denied: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        LOGGER.error("Unexpected error: {}", ex.getMessage(), ex);

        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String message) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status.value()), message);
    }
}
