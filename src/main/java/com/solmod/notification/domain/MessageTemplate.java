package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageTemplate extends Tenanted {

    private String eventSubject;
    private String eventVerb;
    private MessageTemplateStatus messageTemplateStatus = MessageTemplateStatus.ACTIVE;
    private String recipientContextKey;
    private ContentLookupType summaryContentLookupType;
    private String summaryContentKey;
    private ContentLookupType bodyContentLookupType;
    private String bodyContentKey;
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

    public MessageTemplateStatus getStatus() {
        return messageTemplateStatus;
    }

    public void setMessageTemplateStatus(MessageTemplateStatus messageTemplateStatus) {
        this.messageTemplateStatus = messageTemplateStatus;
    }

    public String getRecipientContextKey() {
        return recipientContextKey;
    }

    public void setRecipientContextKey(String recipientContextKey) {
        this.recipientContextKey = recipientContextKey;
    }

    public ContentLookupType getSummaryContentLookupType() {
        return summaryContentLookupType;
    }

    public void setSummaryContentLookupType(ContentLookupType summaryContentLookupType) {
        this.summaryContentLookupType = summaryContentLookupType;
    }

    public String getSummaryContentKey() {
        return summaryContentKey;
    }

    public void setSummaryContentKey(String summaryContentLookupKey) {
        this.summaryContentKey = summaryContentLookupKey;
    }

    public ContentLookupType getBodyContentLookupType() {
        return bodyContentLookupType;
    }

    public void setBodyContentLookupType(ContentLookupType bodyContentLookupType) {
        this.bodyContentLookupType = bodyContentLookupType;
    }

    public String getBodyContentKey() {
        return bodyContentKey;
    }

    public void setBodyContentKey(String bodyContentLookupKey) {
        this.bodyContentKey = bodyContentLookupKey;
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
        return Objects.equals(messageTemplateStatus, that.messageTemplateStatus);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result + (eventSubject != null ? eventSubject.hashCode() : 0);
        result = result + (eventVerb != null ? eventVerb.hashCode() : 0);
        result = result + (messageTemplateStatus != null ? messageTemplateStatus.hashCode() : 0);
        result = result + (recipientContextKey != null ? recipientContextKey.hashCode() : 0);
        result = result + (summaryContentLookupType != null ? summaryContentLookupType.hashCode() : 0);
        result = result + (summaryContentKey != null ? summaryContentKey.hashCode() : 0);
        result = result + (bodyContentLookupType != null ? bodyContentLookupType.hashCode() : 0);
        result = result + (bodyContentKey != null ? bodyContentKey.hashCode() : 0);
        result = result + (deliveryCriteria != null ? deliveryCriteria.hashCode() : 0);

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
