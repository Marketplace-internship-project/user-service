package io.hohichh.marketplace.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMethodArgumentException extends RuntimeException {

    public InvalidMethodArgumentException(String message) {
        super(message);
    }

}