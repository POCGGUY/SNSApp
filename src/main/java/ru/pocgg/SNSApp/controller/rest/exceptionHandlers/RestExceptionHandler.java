package ru.pocgg.SNSApp.controller.rest.exceptionHandlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.pocgg.SNSApp.DTO.display.ErrorDisplayDTO;
import ru.pocgg.SNSApp.model.exceptions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDisplayDTO> handleJsonParseException(HttpMessageNotReadableException e) {
        return buildErrorResponse("JSON_PARSE_ERROR",
                e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDisplayDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.warn("Entity not found: {}", ex.getMessage());
        return buildErrorResponse("ENTITY_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDisplayDTO> handleGeneralException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage());
        return buildErrorResponse("INTERNAL_SERVER_ERROR",
                "An expected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FoundUniqueExistingValuesException.class)
    public ResponseEntity<ErrorDisplayDTO> handleFoundUniqueExistingValuesException(FoundUniqueExistingValuesException ex) {
        logger.warn("Unique constraint error: {}", ex.getMessage());
        return buildErrorResponse("UNIQUE_CONFLICT", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadEnumException.class)
    public ResponseEntity<ErrorDisplayDTO> handleBadEnumException(BadEnumException ex) {
        logger.warn("Enum error: {}", ex.getMessage());
        return buildErrorResponse("INVALID_ENUM", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDisplayDTO> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse("ACCESS_DENIED", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDisplayDTO> handleConstraintViolation(ConstraintViolationException ex) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> v : violations) {
            sb.append(v.getPropertyPath()).append(": ").append(v.getMessage()).append("; ");
        }
        return buildErrorResponse("VALIDATION_ERROR", sb.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDisplayDTO> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Wrong username or password");
        return buildErrorResponse("AUTHENTICATION_FAILED", "Wrong username or password",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDisplayDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.warn("validation failed: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        StringBuilder sb = new StringBuilder();
        fieldErrors.forEach((field, msg) -> sb.append(field).append(": ").append(msg).append("; "));

        return buildErrorResponse("VALIDATION_ERROR", sb.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDisplayDTO> handleBadRequest(BadRequestException ex) {
        String errMsg = ex.getMessage();
        logger.warn("Bad request: {}", errMsg);
        return buildErrorResponse("BAD_REQUEST", errMsg, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorDisplayDTO> buildErrorResponse(String error, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                ErrorDisplayDTO.builder()
                        .error(error)
                        .message(message)
                        .build()
        );
    }
}
