package com.solmod.notifications.dispatcher.filter.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public class MessageDelivery {
    private String recipient;
    private Long messageTemplateId;
    private Status status; // TODO
    private String statusMessage; // e.g. Status = failed, message is failure
    private String messageBodyUri;
    private String sender;
    private String messageBody; // TODO: Reminder that this needs to be built somewhere, but not sure where yet
    private Map<String, String> metadata;

    enum Status {

        // MessageDelivery statuses
        DELIVERED("V"),
        TRIED("T"),
        FAILED("F"),
        PENDING_CONTEXT("PC"),
        PENDING_DELIVERY("PD");


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
}
