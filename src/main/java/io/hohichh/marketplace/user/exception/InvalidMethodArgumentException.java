/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Define custom exception for invalid method arguments (400 Bad Request)
 */

package io.hohichh.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Custom runtime exception indicating that a method argument was invalid.
 * <p>
 * This exception is mapped to an HTTP 400 (Bad Request) response status.
 * It is typically used for business logic validation failures at the service level,
 * distinct from {@link org.springframework.web.bind.MethodArgumentNotValidException}
 * which handles JSR-303 bean validation at the controller level.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMethodArgumentException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidMethodArgumentException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public InvalidMethodArgumentException(String message) {
        super(message);
    }

}