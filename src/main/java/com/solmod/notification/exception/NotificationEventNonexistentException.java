package com.solmod.notification.exception;

import com.solmod.notification.engine.domain.NotificationEvent;

public class NotificationEventNonexistentException extends Exception {
    private final NotificationEvent context;

    public NotificationEventNonexistentException(NotificationEvent context, String message) {
        super(message);
        this.context = context;
    }

    public NotificationEvent getContext() {
        return context;
    }
}
