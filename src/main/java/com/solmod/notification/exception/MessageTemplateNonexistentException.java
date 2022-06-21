package com.solmod.notification.exception;

public class MessageTemplateNonexistentException extends Exception {
    private final Object thing;

    public MessageTemplateNonexistentException(Object thing, String message) {
        super(message);
        this.thing = thing;
    }

    public Object getThing() {
        return thing;
    }
}
