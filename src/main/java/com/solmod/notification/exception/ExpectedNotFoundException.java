package com.solmod.notification.exception;


public class ExpectedNotFoundException extends Exception {
    private final String componentType;
    private final Long componentId;

    public ExpectedNotFoundException(String componentType, Long componentId) {
        super(componentType + " ID " + componentId + " not found");
        this.componentType = componentType;
        this.componentId = componentId;
    }

    public String getComponentType() {
        return componentType;
    }

    public Long getComponentId() {
        return componentId;
    }
}
