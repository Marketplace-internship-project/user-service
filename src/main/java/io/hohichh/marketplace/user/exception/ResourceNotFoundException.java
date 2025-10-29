/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Define custom exception for resources not found (404 Not Found)
 */

package io.hohichh.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Custom runtime exception indicating that a requested resource could not be found.
 * <p>
 * This exception is mapped to an HTTP 404 (Not Found) response status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@link ResourceNotFoundException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

}