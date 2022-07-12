package com.solmod.notification.domain;

public class NotificationDelivery extends Audited {
    private String recipient;
    private Long messageTemplateId;
    private Status status;
    private String messageBodyUri;
    private String deliveryProcessKey;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Long getMessageTemplateId() {
        return messageTemplateId;
    }

    public void setMessageTemplateId(Long messageTemplateId) {
        this.messageTemplateId = messageTemplateId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessageBodyUri() {
        return messageBodyUri;
    }

    public void setMessageBodyUri(String messageBodyUri) {
        this.messageBodyUri = messageBodyUri;
    }

    public String getDeliveryProcessKey() {
        return deliveryProcessKey;
    }

    public void setDeliveryProcessKey(String deliveryProcessKey) {
        this.deliveryProcessKey = deliveryProcessKey;
    }
}
