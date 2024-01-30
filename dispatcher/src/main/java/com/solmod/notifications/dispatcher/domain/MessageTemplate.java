package com.solmod.notifications.dispatcher.domain;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
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
     * @param allDuplicateDeliveries List of {@link MessageDelivery} representing all delivered and any pending deliveries
     * @return boolean
     */
    public DeliveryPermission applySendRules(List<MessageDelivery> allDuplicateDeliveries) {

        // If there've been no previous duplicate deliveries, call it good
        if (allDuplicateDeliveries == null || allDuplicateDeliveries.isEmpty()) {
            return DeliveryPermission.SEND_NOW_PERMISSION;
        }

        // Compare for deliveries numbering fewer than maxSend, if there is a maxSend
        if (hasMaxSendRules() && allDuplicateDeliveries.size() >= getMaxSend()) {
            return new DeliveryPermission(DeliveryPermission.Verdict.SEND_NEVER,
                    String.format("Recipient received the max duplicates (%s), per template rules", allDuplicateDeliveries.size()));
        }

        // Ensure not sending duplicate until after resendInterval is met
        if (hasResendInterval()) {
            MessageDelivery latestDelivery = allDuplicateDeliveries.get(0); // First in list is latest
            // Compare the latest delivery as being at least earlier than the resendInterval
            Date latestDeliveryDate = ObjectUtils.defaultIfNull(latestDelivery.getDateCompleted(), latestDelivery.getDateCreated());
            Date earliestValidSend = DateUtils.addMinutes(latestDeliveryDate, getResendInterval());
            Date now = new Date();

            if (!earliestValidSend.before(now)) {
                return new DeliveryPermission(DeliveryPermission.Verdict.SEND_NEVER,
                        "Recipient has received this message within the resendInterval, per template rules");
            }
        }

        return DeliveryPermission.SEND_NOW_PERMISSION;
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
