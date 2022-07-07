package com.solmod.notification.exception;

import com.solmod.notification.domain.NotificationEvent;

public class NotificationContextAlreadyExistsException extends Exception {
    private final NotificationEvent context;

    public NotificationContextAlreadyExistsException(NotificationEvent template, String message) {
        super(message + "\n  Collision with ID " + template);
        this.context = template;
    }

    public NotificationEvent getContext() {
        return context;
    }
}
