package com.int371.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RequestCooldownException extends RuntimeException {
    public RequestCooldownException(String message) {
        super(message);
    }
}