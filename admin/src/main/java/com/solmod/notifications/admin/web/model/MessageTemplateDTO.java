package com.solmod.notifications.admin.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageTemplateDTO {
    private String sender;
    private String recipientAddressContextKey;
    private String messageBodyContentKey;
    // Retries are re-attempts at failed sends
    private Integer maxRetries;
    private Integer minWaitForRetry; // in seconds
    // Resends control how many times a message could possibly be delivered in response to a repeat SolBus event
    private Integer maxSend;
    private Integer resendInterval; // in minutes

}
