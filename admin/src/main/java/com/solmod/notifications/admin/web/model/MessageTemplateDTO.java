package com.solmod.notifications.admin.web.model;

import lombok.Data;

@Data
public class MessageTemplateDTO {
    private DeliveryCriterionSetDTO deliveryCriteria;
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
