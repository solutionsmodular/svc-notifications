package com.solmod.notification.exception;

import com.solmod.notification.domain.NotificationEvent;

public class NotificationContextNonexistentException extends Exception {
    private final NotificationEvent context;

    public NotificationContextNonexistentException(NotificationEvent context, String message) {
        super(message);
        this.context = context;
    }

    public NotificationEvent getContext() {
        return context;
    }
}
