/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Define custom exception for resource creation conflicts (409 Conflict)
 */

package io.hohichh.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Custom runtime exception indicating that an attempt to create a resource
 * failed because the resource already exists or conflicts with an existing one.
 * <p>
 * This exception is mapped to an HTTP 409 (Conflict) response status.
 * It is commonly used for violations of unique constraints, such as a duplicate email.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceCreationConflictException extends RuntimeException {

    /**
     * Constructs a new {@link ResourceCreationConflictException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ResourceCreationConflictException(String message) {
        super(message);
    }

}