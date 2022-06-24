package com.solmod.notification.admin.data;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.MessageTemplateStatus;

import java.util.Objects;

/**
 * Besides id, this class encapsulates a minimum description of a MessageTemplate which cannot occur more than once.
 * The implications of failing to enforce this include unintentional duplication of notifications to deliver on an event
 */
public class UniqueMessageTemplateId {
    Long tenantId;
    String eventSubject;
    String eventVerb;
    String recipientAddress;
    String contentKey;


    public UniqueMessageTemplateId(Long tenantId, String eventSubject, String eventVerb, String recipientContextKey, String contentKey) {
        this.tenantId = tenantId;
        this.eventSubject = eventSubject;
        this.eventVerb = eventVerb;
        this.recipientAddress = recipientContextKey;
        this.contentKey = contentKey;
    }

    public static UniqueMessageTemplateId from(MessageTemplate template) {
        return new UniqueMessageTemplateId(template.getTenantId(), template.getEventSubject(), template.getEventVerb(), template.getRecipientContextKey(), template.getContentKey());
    }

    public MessageTemplate toMessageTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setTenantId(this.tenantId);
        template.setEventSubject(this.eventSubject);
        template.setEventVerb(this.eventVerb);
        template.setRecipientContextKey(this.recipientAddress);
        template.setContentKey(this.contentKey);
        // Uniqueness only counts for what is active at a given time, so Active is always a criterion
        template.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        return template;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += tenantId != null ? tenantId.hashCode() : 0;
        hash += eventSubject != null ? eventSubject.hashCode() : 0;
        hash += eventVerb != null ? eventVerb.hashCode() : 0;
        hash += recipientAddress != null ? recipientAddress.hashCode() : 0;
        hash += contentKey != null ? contentKey.hashCode() : 0;

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UniqueMessageTemplateId)) {
            return false;
        }
        UniqueMessageTemplateId that = (UniqueMessageTemplateId) obj;

        return Objects.equals(this.tenantId, that.tenantId) &&
                Objects.equals(this.eventSubject, that.eventSubject) &&
                Objects.equals(this.recipientAddress, that.recipientAddress) &&
                Objects.equals(this.contentKey, that.contentKey) &&
                Objects.equals(this.eventVerb, that.eventVerb);
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
