package com.solmod.notification.exception;

import com.solmod.notification.domain.NotificationContext;

public class NotificationContextNonexistentException extends Exception {
    private final NotificationContext context;

    public NotificationContextNonexistentException(NotificationContext context, String message) {
        super(message);
        this.context = context;
    }

    public NotificationContext getContext() {
        return context;
    }
}
