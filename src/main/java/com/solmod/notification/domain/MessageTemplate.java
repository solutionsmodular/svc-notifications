package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageTemplate extends Tenanted {

    private String eventSubject;
    private String eventVerb;
    private Status status = Status.ACTIVE;
    private String recipientContextKey;
    private ContentLookupType contentLookupType;
    private String contentKey;
    private Map<String, Object> deliveryCriteria = new HashMap<>();

    public String getEventSubject() {
        return eventSubject;
    }

    public void setEventSubject(String eventSubject) {
        this.eventSubject = eventSubject;
    }

    public String getEventVerb() {
        return eventVerb;
    }

    public void setEventVerb(String eventVerb) {
        this.eventVerb = eventVerb;
    }

    public Status getStatus() {
        return status;
    }

    public void setMessageTemplateStatus(Status status) {
        this.status = status;
    }

    public String getRecipientContextKey() {
        return recipientContextKey;
    }

    public void setRecipientContextKey(String recipientContextKey) {
        this.recipientContextKey = recipientContextKey;
    }

    public Status getMessageTemplateStatus() {
        return status;
    }

    public ContentLookupType getContentLookupType() {
        return contentLookupType;
    }

    public void setContentLookupType(ContentLookupType contentLookupType) {
        this.contentLookupType = contentLookupType;
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

        if (!Objects.equals(eventSubject, that.eventSubject)) return false;
        if (!Objects.equals(eventVerb, that.eventVerb)) return false;
        if (!Objects.equals(recipientContextKey, that.recipientContextKey)) return false;
        if (!Objects.equals(contentLookupType, that.contentLookupType)) return false;
        if (!Objects.equals(contentKey, that.contentKey)) return false;
        if (!Objects.equals(deliveryCriteria, that.deliveryCriteria)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (eventSubject != null ? eventSubject.hashCode() : 0);
        result += (eventVerb != null ? eventVerb.hashCode() : 0);
        result += (status != null ? status.hashCode() : 0);
        result += (recipientContextKey != null ? recipientContextKey.hashCode() : 0);
        result += (contentLookupType != null ? contentLookupType.hashCode() : 0);
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
