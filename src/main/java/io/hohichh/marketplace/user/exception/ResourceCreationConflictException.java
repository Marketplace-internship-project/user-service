package io.hohichh.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceCreationConflictException extends RuntimeException {
    public ResourceCreationConflictException(String message) {
        super(message);
    }

}