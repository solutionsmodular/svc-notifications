package com.solmod.notification.exception;

import com.solmod.notification.domain.NotificationTrigger;

public class NotificationTriggerNonexistentException extends DBRequestFailureException {
    private final NotificationTrigger context;

    public NotificationTriggerNonexistentException(NotificationTrigger context, String message) {
        super(message);
        this.context = context;
    }

    public NotificationTrigger getContext() {
        return context;
    }
}
