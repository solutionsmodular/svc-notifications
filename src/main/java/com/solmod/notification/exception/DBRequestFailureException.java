package com.solmod.notification.exception;

public class DBRequestFailureException extends Exception {
    public DBRequestFailureException() {
        super();
    }

    public DBRequestFailureException(String message) {
        super(message);
    }
}
