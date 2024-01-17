package com.solmod.notifications.admin.web.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDeliveryPreferencesDTO {
    private UUID userId;
    private String messageClass; // Comma-delimited list of the classes permitted
    private String sender;
    private Integer sendWindowStart; // hour of day
    private Integer sendWindowEnd; // hour of day
    private String timezone;
    private String recipientAddress;
    private Integer resendInterval; // In minutes
}
