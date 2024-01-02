package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity(name = "EmailMessageTemplates")
@DiscriminatorValue("E")
public class EmailMessageTemplate extends MessageTemplate {
    private String messageSubjectContentKey;

    public String getMessageSubjectContentKey() {
        return messageSubjectContentKey;
    }

    public void setMessageSubjectContentKey(String messageSubjectContentKey) {
        this.messageSubjectContentKey = messageSubjectContentKey;
    }
}
