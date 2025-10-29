/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement global exception handling for the application's REST controllers
 */

package io.hohichh.marketplace.user.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


/**
 * Global exception handler for the REST API.
 * This class uses {@link RestControllerAdvice} to intercept exceptions thrown by
 * controllers and translate them into standardized, user-friendly HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * A simple record to represent a standardized error message in the API response.
     *
     * @param message The error message.
     */
    public record ErrorResponse(String message) {
    }


    /**
     * Handles {@link ResourceNotFoundException}.
     * Returns an HTTP 404 (Not Found) response with the exception's message.
     *
     * @param ex The caught {@link ResourceNotFoundException}.
     * @return An {@link ErrorResponse} containing the error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }


    /**
     * Handles {@link ResourceCreationConflictException}.
     * Returns an HTTP 409 (Conflict) response, typically used when a resource
     * (like a user with a unique email) already exists.
     *
     * @param ex The caught {@link ResourceCreationConflictException}.
     * @return An {@link ErrorResponse} containing the conflict message.
     */
    @ExceptionHandler(ResourceCreationConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ResourceCreationConflictException ex) {
        return new ErrorResponse(ex.getMessage());
    }


    /**
     * Handles {@link InvalidMethodArgumentException}.
     * Returns an HTTP 400 (Bad Request) response.
     *
     * @param ex The caught {@link InvalidMethodArgumentException}.
     * @return An {@link ErrorResponse} containing the error message.
     */
    @ExceptionHandler(InvalidMethodArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidMethodArgument(InvalidMethodArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles {@link MethodArgumentNotValidException}, which is thrown by Spring Validation
     * (e.g., @Valid) when DTO validation fails.
     * Returns an HTTP 400 (Bad Request) response containing a map of violated fields
     * and their corresponding error messages.
     *
     * @param ex The caught {@link MethodArgumentNotValidException}.
     * @return A Map where the key is the field name and the value is the validation error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


    /**
     * A general catch-all exception handler for any unhandled exceptions.
     * Logs the full exception stack trace for debugging.
     * Returns an HTTP 500 (Internal Server Error) with a generic, non-specific
     * message to avoid leaking sensitive implementation details.
     *
     * @param ex The caught {@link Exception}.
     * @return A generic {@link ErrorResponse}.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {

        log.error("Unhandled exception occurred: ", ex);

        return new ErrorResponse("An unexpected internal server error occurred.");
    }
}