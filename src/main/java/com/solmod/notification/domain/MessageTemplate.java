package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.Objects;

/**
 * A MessageTemplate is the component which, for a given {@link MessageConfig}, specifies a specific message to deliver.
 * By "specific", we refer to the channel through which the message should be delivered, the CMS content key for the
 * message body, and where in the Event Context it should find the address for the recipient.
 * In other words, where a {@link MessageConfig} would indicate subject/verb and conditions specifying, say, a scope
 * of "Order Placed - Customer Sponsor", indicating a notification to be sent to the sponsor of someone who placed
 * an order,... The MessageTemplate indicates the actual shortened body to be sent, onto a timeline, or via SMS, and
 * indicates the more verbose message body like that would be used for an email.
 * All configurations found will be processed.
 */
public class MessageTemplate extends Tenanted {

    private Long messageConfigId;
    private Status status;
    private String recipientContextKey;
    private MessageSender messageSender;
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

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
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
        if (!Objects.equals(messageSender, that.messageSender)) return false;
        if (!Objects.equals(contentKey, that.contentKey)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (status != null ? status.hashCode() : 0);
        result += (recipientContextKey != null ? recipientContextKey.hashCode() : 0);
        result += (messageSender != null ? messageSender.hashCode() : 0);
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
