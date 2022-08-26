package com.solmod.notification.engine.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageConfig extends Audited {

    private Long notificationEventId;
    private Status status;
    private Map<String, String> deliveryCriteria = new HashMap<>();
    private List<MessageTemplate> messageTemplates;

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

    public Map<String, String> getDeliveryCriteria() {
        return deliveryCriteria;
    }

    public void setDeliveryCriteria(Map<String, String> deliveryCriteria) {
        this.deliveryCriteria = deliveryCriteria != null ? deliveryCriteria : new HashMap<>();
    }

    public void addDeliveryCriteria(String contextKeyName, String contextValue) {
        this.deliveryCriteria.put(contextKeyName, contextValue);
    }

    public List<MessageTemplate> getMessageTemplates() {
        return messageTemplates;
    }

    public void setMessageTemplates(List<MessageTemplate> messageTemplates) {
        this.messageTemplates = messageTemplates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!super.equals(o))
            return false;

        MessageConfig that = (MessageConfig) o;

        if (!Objects.equals(notificationEventId, that.notificationEventId)) return false;
        if (!Objects.equals(status, that.status)) return false;
        return Objects.equals(deliveryCriteria, that.deliveryCriteria);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (status != null ? status.hashCode() : 0);
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
