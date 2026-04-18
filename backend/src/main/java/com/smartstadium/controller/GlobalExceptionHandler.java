package com.smartstadium.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Converts exceptions into RFC 7807 ProblemDetail objects,
 * ensuring no stack traces or internal details leak to clients.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(
        GlobalExceptionHandler.class
    );

    /**
     * Handles invalid input parameters (e.g., invalid zone names).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        logger.warn(
            "Bad request to {}: {}",
            request.getRequestURI(),
            ex.getMessage()
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    /**
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(
        MissingServletRequestParameterException ex,
        HttpServletRequest request
    ) {
        String message = "Missing required parameter: " + ex.getParameterName();
        logger.warn(
            "Missing parameter in request to {}: {}",
            request.getRequestURI(),
            message
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            message
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    /**
     * Handles type mismatch in request parameters.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        String message =
            "Invalid value for parameter '" +
            ex.getName() +
            "': " +
            ex.getValue();
        logger.warn(
            "Type mismatch in request to {}: {}",
            request.getRequestURI(),
            message
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            message
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    /**
     * Handles validation exceptions for request bodies.
     */
    @ExceptionHandler(
        org.springframework.web.bind.MethodArgumentNotValidException.class
    )
    public ProblemDetail handleValidationExceptions(
        org.springframework.web.bind.MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        logger.warn(
            "Validation failed for request to {}: {}",
            request.getRequestURI(),
            ex.getMessage()
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed for one or more fields."
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    /**
     * Handles 404 Not Found for unmapped endpoints.
     */
    @ExceptionHandler(
        org.springframework.web.servlet.resource.NoResourceFoundException.class
    )
    public ProblemDetail handleNotFound(
        org.springframework.web.servlet.resource.NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        logger.warn(
            "Resource not found at {}: {}",
            request.getRequestURI(),
            ex.getMessage()
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Resource not found"
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    /**
     * Catches all unhandled exceptions to prevent stack trace leakage.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        logger.error(
            "Unhandled exception at {}: {}",
            request.getRequestURI(),
            ex.getMessage(),
            ex
        );
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An internal error occurred. Please try again later."
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }
}
