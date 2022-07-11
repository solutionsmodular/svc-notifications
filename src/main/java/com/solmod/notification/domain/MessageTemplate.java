package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageTemplate extends Audited {

    private Long notificationEventId;
    private Status status;
    private String recipientContextKey;
    private MessageContentPurpose messageContentPurpose;
    private String contentKey;
    private Map<String, Object> deliveryCriteria = new HashMap<>();

    public Status getStatus() {
        return status;
    }

    public Long getNotificationEventId() {
        return notificationEventId;
    }

    public void setNotificationEventId(Long notificationEventId) {
        this.notificationEventId = notificationEventId;
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

    public Map<String, Object> getDeliveryCriteria() {
        return deliveryCriteria;
    }

    public void setDeliveryCriteria(Map<String, Object> deliveryCriteria) {
        this.deliveryCriteria = deliveryCriteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!super.equals(o))
            return false;

        MessageTemplate that = (MessageTemplate) o;

        if (!Objects.equals(notificationEventId, that.notificationEventId)) return false;
        if (!Objects.equals(recipientContextKey, that.recipientContextKey)) return false;
        if (!Objects.equals(messageContentPurpose, that.messageContentPurpose)) return false;
        if (!Objects.equals(contentKey, that.contentKey)) return false;
        if (!Objects.equals(status, that.status)) return false;
        return Objects.equals(deliveryCriteria, that.deliveryCriteria);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (status != null ? status.hashCode() : 0);
        result += (recipientContextKey != null ? recipientContextKey.hashCode() : 0);
        result += (messageContentPurpose != null ? messageContentPurpose.hashCode() : 0);
        result += (contentKey != null ? contentKey.hashCode() : 0);
        result += (deliveryCriteria != null ? deliveryCriteria.hashCode() : 0);

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
