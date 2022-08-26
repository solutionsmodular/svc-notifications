package com.solmod.notification.engine.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.Map;
import java.util.Objects;

public class MessageTemplate extends Audited {

    private Long messageConfigId;
    private Status status;
    private String recipientContextKey;
    private MessageContentPurpose messageContentPurpose;
    private String contentKey;

    public Status getStatus() {
        return status;
    }

    public Long getMessageConfigId() {
        return messageConfigId;
    }

    public void setMessageConfigId(Long messageConfigId) {
        this.messageConfigId = messageConfigId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRecipientContextKey() {
        return recipientContextKey;
    }

    public void setRecipientContextKey(String recipientContextKey) {
        this.recipientContextKey = recipientContextKey;
    }

    public MessageContentPurpose getMessageContentPurpose() {
        return messageContentPurpose;
    }

    public void setMessageContentPurpose(MessageContentPurpose messageContentPurpose) {
        this.messageContentPurpose = messageContentPurpose;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!super.equals(o))
            return false;

        MessageTemplate that = (MessageTemplate) o;

        if (!Objects.equals(messageConfigId, that.messageConfigId)) return false;
        if (!Objects.equals(recipientContextKey, that.recipientContextKey)) return false;
        if (!Objects.equals(messageContentPurpose, that.messageContentPurpose)) return false;
        if (!Objects.equals(contentKey, that.contentKey)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (status != null ? status.hashCode() : 0);
        result += (recipientContextKey != null ? recipientContextKey.hashCode() : 0);
        result += (messageContentPurpose != null ? messageContentPurpose.hashCode() : 0);
        result += (contentKey != null ? contentKey.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        try {
            return ObjectUtils.stringify(this);
        } catch (StringifyException e) {
            return Objects.toString(this);
        }
    }
}
