package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.*;

/**
 * Where a {@link NotificationEvent} specifies the subject/verb of a message bus event message, the MessageConfig
 * introduces a point at which the process of (event -> message delivery) can branch, based on criteria. In this way,
 * we can configure a message to go out to an order owner and to her sponsor based on an order being placed, or payment
 * failing.
 */
public class MessageConfig extends Audited {

    private Long notificationEventId;
    private String name;
    private Status status;
    private Map<String, String> deliveryCriteria = new HashMap<>();
    private List<MessageTemplate> messageTemplates = new ArrayList<>();

    public Status getStatus() {
        return status;
    }

    public Long getNotificationEventId() {
        return notificationEventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (messageTemplates != null) {
            this.messageTemplates = messageTemplates;
        }
    }

    public void addMessageTemplate(MessageTemplate messageTemplate) {
        if (messageTemplate != null) {
            this.messageTemplates.add(messageTemplate);
        }
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
