package com.appweb.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "An error has occurred")
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
