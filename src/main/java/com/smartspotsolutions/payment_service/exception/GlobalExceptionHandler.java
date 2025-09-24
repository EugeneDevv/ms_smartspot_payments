package com.smartspotsolutions.payment_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final HttpServletRequest request;
    private final MessageSource messageSource;

    // Helper to get the current locale from the request context
    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, Object details, Exception exception) {
        if (status.is5xxServerError()) {
            log.error("Internal server error for URI {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        } else {
            log.warn("Handled exception for URI {}: {} - {}", request.getRequestURI(), exception.getClass().getSimpleName(), exception.getMessage());
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Locale locale = getCurrentLocale();
        Map<String, String> violations = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            violations.put(field, violation.getMessage()); // Use default message from ConstraintViolation
        });

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "An invalid parameter was detected. Please review and resend.", violations, exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        Locale locale = getCurrentLocale();
        String paramName = exception.getName();
        Object rejectedValue = exception.getValue();
        Class<?> requiredType = exception.getRequiredType();

        String expectedType = (requiredType != null) ? requiredType.getSimpleName() : "unknown";
        Object[] params = new Object[]{rejectedValue, paramName, expectedType};
        String message = messageSource.getMessage("common.validation.invalid_param_type", params, locale);
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", paramName);
        details.put("rejectedValue", rejectedValue);
        details.put("expectedType", expectedType);

        if (requiredType != null && requiredType.isEnum()) {
            Object[] enumConstants = requiredType.getEnumConstants();
            details.put("allowedValues", Arrays.toString(enumConstants));
            message += " " + messageSource.getMessage("common.validation.invalid_input", new Object[]{Arrays.toString(enumConstants)}, locale);
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, details, exception);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(UsernameNotFoundException exception) {
        // Assuming this message is already well-defined and user-facing
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVerificationToken(InvalidVerificationTokenException exception) {
        // Assuming this message is already well-defined and user-facing
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(VerificationException.class)
    public ResponseEntity<ApiErrorResponse> handleVerificationException(VerificationException exception) {
        // Assuming this message is already well-defined and user-facing
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(VerificationTokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleVerificationTokenExpiredException(VerificationTokenExpiredException exception) {
        // Assuming this message is already well-defined and user-facing
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        Locale locale = getCurrentLocale();
        String message = messageSource.getMessage("common.operation.access_denied", new Object[]{null}, locale);
        return buildErrorResponse(HttpStatus.FORBIDDEN, message, null, exception);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOperation(InvalidOperationException exception) {
        // For custom exceptions, assuming the message is already localized/user-friendly
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyExist(AlreadyExistsException exception) {
        // For custom exceptions, assuming the message is already localized/user-friendly
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        // For custom exceptions, assuming the message is already localized/user-friendly
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), null, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Locale locale = getCurrentLocale();
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()) // DefaultMessage from validation annotations
        );

        String message = messageSource.getMessage("common.validation.failed_body", new Object[]{null}, locale);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, fieldErrors, exception);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        Locale locale = getCurrentLocale();
        List<String> errors = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        String message = messageSource.getMessage("common.validation.param_failed", null, locale);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, errors, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        Locale locale = getCurrentLocale();
        String message = messageSource.getMessage("common.validation.request_malformed", new Object[]{null}, locale);
        Object details = null;

        if (exception.getCause() instanceof InvalidFormatException formatException) {
            String rawPath = formatException.getPathReference();
            String field = extractSimpleFieldName(rawPath);

            Object rejectedValue = formatException.getValue();
            Class<?> targetType = formatException.getTargetType();
            String expectedType = (targetType != null) ? targetType.getSimpleName() : "unknown";

            Object[] params = new Object[]{rejectedValue, field};
            message = messageSource.getMessage("common.validation.invalid_value", params, locale);

            if (targetType != null && targetType.isEnum()) {
                message += " " + messageSource.getMessage("common.validation.invalid_input", new Object[]{Arrays.toString(targetType.getEnumConstants())}, locale);
            }

            details = Map.of(
                    "field", field,
                    "rejectedValue", rejectedValue != null ? rejectedValue.toString() : "null",
                    "expectedType", expectedType
            );
        } else if (exception.getCause() != null) {
            message = messageSource.getMessage("common.validation.request_body_parsing_error", null, locale);
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, details, exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception) {
        Locale locale = getCurrentLocale();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", null, exception);
    }

    private String extractSimpleFieldName(String rawPath) {
        if (rawPath == null || !rawPath.contains("[\"")) return rawPath;
        int start = rawPath.indexOf("[\"");
        int end = rawPath.indexOf("\"]");
        if (start >= 0 && end > start) {
            return rawPath.substring(start + 2, end);
        }
        return rawPath; // fallback
    }
}