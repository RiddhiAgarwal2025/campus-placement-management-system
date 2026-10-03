package com.campusplacement.service;

/** A failure whose message is safe and meaningful to show to the user. */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
