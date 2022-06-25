package com.solmod.notification.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Status {
    // Used by MessageTemplate
    INACTIVE("I"),
    ACTIVE("A"),
    DELETED("D"),
    // Used by Notification Delivery
    DELIVERED("V"),
    CANCELED("C"),
    FAILED("F"),
    PENDING("P");

    private final String code;

    Status(String code) {
        this.code = code;
    }

    public static Status fromCode(String code) {
        for (Status value : values()) {
            if (value.code().equals(code))
                return value;
        }

        return null;
    }
    @JsonValue
    public String code() {
        return code;
    }
}
