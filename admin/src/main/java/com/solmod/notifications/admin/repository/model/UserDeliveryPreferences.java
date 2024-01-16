package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class UserDeliveryPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private UUID userId;

    private String messageClass;
    private String sender;

    private Integer sendWindowStart; // hour of day
    private Integer sendWindowEnd; // hour of day
    private String timezone;
    private String recipientAddress;
    private Integer resendInterval; // In minutes

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user) {
        this.userId = user;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Integer getSendWindowStart() {
        return sendWindowStart;
    }

    public void setSendWindowStart(Integer sendWindowStart) {
        this.sendWindowStart = sendWindowStart;
    }

    public Integer getSendWindowEnd() {
        return sendWindowEnd;
    }

    public void setSendWindowEnd(Integer sendWindowEnd) {
        this.sendWindowEnd = sendWindowEnd;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public Integer getResendInterval() {
        return resendInterval;
    }

    public void setResendInterval(Integer resendInterval) {
        this.resendInterval = resendInterval;
    }
}
