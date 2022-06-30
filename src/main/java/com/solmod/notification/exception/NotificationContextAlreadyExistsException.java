package com.solmod.notification.exception;

import com.solmod.notification.domain.NotificationContext;

public class NotificationContextAlreadyExistsException extends Exception {
    private final NotificationContext context;

    public NotificationContextAlreadyExistsException(NotificationContext template, String message) {
        super(message + "\n  Collision with ID " + template);
        this.context = template;
    }

    public NotificationContext getContext() {
        return context;
    }
}
