package com.solmod.notification.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Status {
    // Used by MessageTemplate
    INACTIVE("I"),
    ACTIVE("A"),
    DELETED("D"),

    // MessageTrigger statuses
    NO_OP("NO"), // If, in the end, no MessageTemplates qualify/exist to send
    PROCESSING("P"),
    COMPLETE("CP"),

    // MessageDelivery statuses
    DELIVERED("V"),
    FAILED("F"),
    PENDING_CONTEXT("PC"),
    PENDING_PERMISSION("PP"),
    PENDING_DELIVERY("PD");

    @JsonValue
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
