package com.solmod.notification.exception;

import com.solmod.notification.domain.MessageTemplate;

public class MessageTemplateAlreadyExistsException extends Exception {
    private final MessageTemplate template;

    public MessageTemplateAlreadyExistsException(MessageTemplate template, String message) {
        super(message + "\n  Collision with ID " + template);
        this.template = template;
    }

    public MessageTemplate getTemplate() {
        return template;
    }
}
