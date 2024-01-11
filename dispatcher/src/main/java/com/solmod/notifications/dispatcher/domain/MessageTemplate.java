package com.solmod.notifications.dispatcher.domain;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.List;

public class MessageTemplate extends MessageTemplateDTO {

    /**
     * True indicates there are send rules to validate
     *
     * @return boolean
     */
    public boolean hasSendRules() {
        return hasMaxSendRules() || hasRetryInterval();
    }

    /**
     * Determine if the rules of this template are met, given the matching deliveries made before
     *
     * @param allDeliveries List of {@link MessageDelivery} representing all delivered and any pending deliveries
     * @return boolean
     */
    public boolean meetsSendRules(List<MessageDelivery> allDeliveries) {

        // If there've been no previous deliveries, call it good
        if (allDeliveries == null || allDeliveries.isEmpty()) {
            return true;
        }

        // Compare for deliveries numbering fewer than maxSend, if there is a maxSend
        boolean meetsRules = !hasMaxSendRules() || allDeliveries.size() < getMaxSend();

        // Unless already !meetsRules and has retryInterval, compare
        if (meetsRules && hasRetryInterval()) {
            MessageDelivery latestDelivery = allDeliveries.get(0); // First in list is latest
            // Compare the latest delivery as being at least earlier than the resendInterval
            Date latestDeliveryDate = ObjectUtils.defaultIfNull(latestDelivery.getDateCompleted(), latestDelivery.getDateCreated());
            Date earliestSend = DateUtils.addMinutes(latestDeliveryDate, getResendInterval());
            Date now = new Date();
            meetsRules = earliestSend.before(now);
        }

        return meetsRules;
    }

    private boolean hasRetryInterval() {
        return getResendInterval() != null && !getResendInterval().equals(0);
    }

    private boolean hasMaxSendRules() {
        return getMaxSend() != null && !getMaxSend().equals(0);
    }
}
