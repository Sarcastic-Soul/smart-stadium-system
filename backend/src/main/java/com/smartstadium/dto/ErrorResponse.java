package com.smartstadium.dto;

import java.time.Instant;

/**
 * Standardized error response DTO for API error handling.
 * Ensures no internal details or stack traces leak to clients.
 *
 * @param status    the HTTP status code
 * @param error     the error type description
 * @param message   a user-friendly error message
 * @param path      the request path that triggered the error
 * @param timestamp when the error occurred
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
}
