package com.solmod.notifications.admin.repository.model;

import com.solmod.notifications.admin.web.model.ContentKeySetDTO;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity(name = "EmailMessageTemplates")
@DiscriminatorValue("E")
public class EmailMessageTemplate extends MessageTemplate {
    private String messageSubjectContentKey;

    @Override
    public ContentKeySetDTO toContentKeySet() {
        ContentKeySetDTO contentKeySet = super.toContentKeySet();
        contentKeySet.addContentKey("messageSubjectContentKey", messageSubjectContentKey);
        return contentKeySet;
    }

    public String getMessageSubjectContentKey() {
        return messageSubjectContentKey;
    }

    public void setMessageSubjectContentKey(String messageSubjectContentKey) {
        this.messageSubjectContentKey = messageSubjectContentKey;
    }
}
