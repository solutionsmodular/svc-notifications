package com.solmod.notification.domain;

import java.util.HashMap;
import java.util.Map;

public class NotificationDelivery extends Audited {
    private String recipient;
    private Long messageTemplateId;
    private Status status;
    private String messageBodyUri;
    private Map<String, Object> context = new HashMap<>();

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

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
