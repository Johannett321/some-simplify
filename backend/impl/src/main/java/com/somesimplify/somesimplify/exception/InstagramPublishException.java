package com.somesimplify.somesimplify.exception;

public class InstagramPublishException extends RuntimeException {

    public InstagramPublishException(String message) {
        super(message);
    }

    public InstagramPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
