package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

/**
 * Associate a certain message content to be delivered via a particular sender
 */
@Entity(name = "BasicMessageTemplates")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="template_type", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("null")
public class MessageTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_theme_id")
    private Theme theme;
    private String sender;
    private String recipientAddressContextKey;
    private String messageBodyContentKey;
    private Integer maxRetries;
    private Integer minWaitForRetry; // in seconds
    private Integer maxSend;
    private Integer resendInterval;
    private Integer resendIntervalPeriod; // Use Calendar constants

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipientAddressContextKey() {
        return recipientAddressContextKey;
    }

    public void setRecipientAddressContextKey(String recipientAddressContextKey) {
        this.recipientAddressContextKey = recipientAddressContextKey;
    }

    public String getMessageBodyContentKey() {
        return messageBodyContentKey;
    }

    public void setMessageBodyContentKey(String messageBodyContentKey) {
        this.messageBodyContentKey = messageBodyContentKey;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getMinWaitForRetry() {
        return minWaitForRetry;
    }

    public void setMinWaitForRetry(Integer minWaitForRetry) {
        this.minWaitForRetry = minWaitForRetry;
    }

    public Integer getMaxSend() {
        return maxSend;
    }

    public void setMaxSend(Integer maxSend) {
        this.maxSend = maxSend;
    }

    public Integer getResendInterval() {
        return resendInterval;
    }

    public void setResendInterval(Integer resendInterval) {
        this.resendInterval = resendInterval;
    }

    public Integer getResendIntervalPeriod() {
        return resendIntervalPeriod;
    }

    public void setResendIntervalPeriod(Integer intervalPeriod) {
        this.resendIntervalPeriod = intervalPeriod;
    }
}
