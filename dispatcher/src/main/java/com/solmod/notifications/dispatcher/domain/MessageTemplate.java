package com.solmod.notifications.dispatcher.domain;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class MessageTemplate extends MessageTemplateDTO {

    private Logger log = LoggerFactory.getLogger(MessageTemplate.class);

    /**
     * True indicates there are send rules to validate
     *
     * @return boolean
     */
    public boolean hasSendRules() {
        return hasMaxSendRules() || hasResendInterval();
    }

    /**
     * Determine if the rules of this template are met, given the matching deliveries made before
     *
     * @param allDeliveries List of {@link MessageDelivery} representing all delivered and any pending deliveries
     * @param message {@link SolMessage}
     * @return boolean
     */
    public boolean meetsSendRules(List<MessageDelivery> allDeliveries, SolMessage message) {

        // If there've been no previous deliveries, call it good
        if (allDeliveries == null || allDeliveries.isEmpty()) {
            return true;
        }

        // Compare for deliveries numbering fewer than maxSend, if there is a maxSend
        boolean meetsRules = !hasMaxSendRules() || allDeliveries.size() < getMaxSend();
        if (!meetsRules) {
            log.info("Sending template {} for metadata ID {} would exceed maxSend rules for the template",
                    message.getIdMetadataKey() + "|" + message.getIdMetadataKey(), this.getMessageTemplateID());
        }

        // Unless already !meetsRules and has retryInterval, compare
        if (meetsRules && hasResendInterval()) {
            MessageDelivery latestDelivery = allDeliveries.get(0); // First in list is latest
            // Compare the latest delivery as being at least earlier than the resendInterval
            Date latestDeliveryDate = ObjectUtils.defaultIfNull(latestDelivery.getDateCompleted(), latestDelivery.getDateCreated());
            Date earliestValidSend = DateUtils.addMinutes(latestDeliveryDate, getResendInterval());
            Date now = new Date();
            meetsRules = earliestValidSend.before(now);
            if (!meetsRules) {
                log.info("Sending would violate resend interval");
            }
        }

        return meetsRules;
    }

    private boolean hasResendInterval() {
        return getResendInterval() != null && !getResendInterval().equals(0);
    }

    private boolean hasMaxSendRules() {
        return getMaxSend() != null && !getMaxSend().equals(0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageTemplate comp))
            return false;

        return Objects.equals(comp.getMessageTemplateID(), this.getMessageTemplateID());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
