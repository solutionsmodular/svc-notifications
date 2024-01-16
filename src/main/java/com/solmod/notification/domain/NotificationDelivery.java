package com.solmod.notification.domain;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * A NotificationDelivery encapsulates an instance of a specific message (to deliver, in the process of delivering,
 * delivered, failed to deliver...) to an ultimate recipient at
 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!super.equals(o))
            return false;

        NotificationDelivery that = (NotificationDelivery) o;

        if (!Objects.equals(that.getDeliveryProcessKey(), ((NotificationDelivery) o).getDeliveryProcessKey()))
            return false;
        if (!Objects.equals(that.getRecipient(), ((NotificationDelivery) o).getRecipient()))
            return false;
        if (!Objects.equals(that.getStatus(), ((NotificationDelivery) o).getStatus()))
            return false;
        if (!Objects.equals(that.getMessageBodyUri(), ((NotificationDelivery) o).getMessageBodyUri()))
            return false;

        return Objects.equals(that.getMessageTemplateId(), ((NotificationDelivery) o).getMessageTemplateId());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 7)
                .append(Optional.ofNullable(deliveryProcessKey).orElse("").hashCode())
                .append(Optional.ofNullable(recipient).orElse("").hashCode())
                .append(Optional.ofNullable(messageBodyUri).orElse("").hashCode())
                .append(Optional.ofNullable(messageTemplateId).orElse(0L).hashCode())
                .append(status == null ? 0 : status.hashCode()).build();
    }
}
