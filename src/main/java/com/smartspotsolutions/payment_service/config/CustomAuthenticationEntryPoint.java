package com.smartspotsolutions.payment_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspotsolutions.payment_service.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 1. Log the exception for server-side debugging
        // Use warn level for authentication failures, debug for full stack trace if needed
        log.warn("Authentication failed for request URI: {} - Message: {}", request.getRequestURI(), authException.getMessage());

        // 2. Build an error response object
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()) // HTTP 401 status code
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase()) // "Unauthorized"
                .message("Authentication is required to access this resource or provided credentials are invalid.")
                .path(request.getRequestURI())
                .build();
        // 3. Set the response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8"); // Good practice to specify the encoding

        // 4. Write the JSON error response to the client
        // ObjectMapper handles the serialization and potential IOException
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
