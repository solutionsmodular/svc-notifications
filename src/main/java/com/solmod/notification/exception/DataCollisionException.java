package com.solmod.notification.exception;

public class DataCollisionException extends Exception {
    private final String componentType;
    private final Long collidingId;

    public DataCollisionException(String componentType, Long collidingId) {
        super("Collision with " + componentType + " ID " + collidingId);
        this.collidingId = collidingId;
        this.componentType = componentType;
    }

    public String getComponentType() {
        return componentType;
    }

    public Long getCollidingId() {
        return collidingId;
    }
}
