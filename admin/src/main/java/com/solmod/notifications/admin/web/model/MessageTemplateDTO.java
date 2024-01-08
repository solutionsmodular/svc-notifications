package com.solmod.notifications.admin.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageTemplateDTO {
    private Long messageTemplateID;
    private String sender;
    private String recipientAddressContextKey;
    private ContentKeySetDTO contentKeySet;
    // Retries are re-attempts at failed sends
    private Integer maxRetries;
    private Integer minWaitForRetry; // in seconds
    // Resends control how many times a message could possibly be delivered in response to a repeat SolBus event
    private Integer maxSend;
    private Integer resendInterval; // in minutes

}
