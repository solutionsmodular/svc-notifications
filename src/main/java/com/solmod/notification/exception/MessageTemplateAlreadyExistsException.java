package com.solmod.notification.exception;

import com.solmod.notification.engine.domain.MessageConfig;

public class MessageTemplateAlreadyExistsException extends Exception {
    private final MessageConfig template;

    public MessageTemplateAlreadyExistsException(MessageConfig template, String message) {
        super(message + "\n  Collision with ID " + template);
        this.template = template;
    }

    public MessageConfig getTemplate() {
        return template;
    }
}
