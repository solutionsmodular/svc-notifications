package com.solmod.notification.exception;

import com.solmod.notification.engine.domain.NotificationEvent;

public class NotificationEventAlreadyExistsException extends Exception {
    private final NotificationEvent context;

    public NotificationEventAlreadyExistsException(NotificationEvent template, String message) {
        super(message + "\n  Collision with ID " + template);
        this.context = template;
    }

    public NotificationEvent getContext() {
        return context;
    }
}
