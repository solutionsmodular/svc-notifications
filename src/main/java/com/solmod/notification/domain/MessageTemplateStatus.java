package com.solmod.notification.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MessageTemplateStatus {
    INACTIVE("I"), ACTIVE("A"), DELETED("D");

    private final String code;

    MessageTemplateStatus(String code) {
        this.code = code;
    }

    public static MessageTemplateStatus fromCode(String code) {
        for (MessageTemplateStatus value : values()) {
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
