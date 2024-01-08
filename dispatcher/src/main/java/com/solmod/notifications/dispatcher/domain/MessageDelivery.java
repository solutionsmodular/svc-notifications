package com.solmod.notifications.dispatcher.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.solmod.notifications.admin.web.model.ContentKeySetDTO;
import lombok.Data;

import java.util.Map;

/**
 * Record of a delivery of a message which will archive the delivery from before any attempts to delivery until
 * delivery is complete and any callbacks have been completed.
 * <ol>
 *     <li>When are placeholders replaced?</li>
 *     <li>Consider the context builder process. When does that happen?</li>
 *     <li>Delivery includes metadata. Metadata should include what's needed for recipient, content, and whatever else
 *         might be needed to determine duplicate</li>
 *     <li>A message can be different, depending on the sender. Need to package diff content elements into envelope</li>
 * </ol>
 */
@Data
public class MessageDelivery {
    private String recipientAddyContextKey;
    private Long messageTemplateId;
    private Status status; // TODO
    private String statusMessage; // e.g. Status = failed, message is failure
    private String sender;
    private ContentKeySetDTO contentKeySet;
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
