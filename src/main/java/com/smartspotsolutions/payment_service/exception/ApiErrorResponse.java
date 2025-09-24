package com.smartspotsolutions.payment_service.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error; // e.g., "Unauthorized", "Bad Request"
    private String message; // A more detailed, user-friendly message
    private String path; // The request URI that caused the error
    private Object details; // For validation errors (Map<String, String>) or other specific details (List<String>), or null
}