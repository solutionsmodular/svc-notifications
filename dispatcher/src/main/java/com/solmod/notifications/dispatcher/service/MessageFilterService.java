package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.filter.FilterException;
import com.solmod.notifications.dispatcher.filter.MessageDeliveryFilter;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MessageFilterService {

    private final Set<MessageDeliveryFilter> deliveryFilters;

    @Autowired
    public MessageFilterService(Set<MessageDeliveryFilter> deliveryFilters) {
        this.deliveryFilters = deliveryFilters;
    }

    /**
     * @param messageTemplate {@link MessageTemplate}
     * @param trigger    {@link TriggeringEvent}
     * @return {@link DeliveryPermission} representing the most restrictive of the verdicts as calculated by each filter
     */
    public DeliveryPermission applyDeliveryFilters(MessageTemplate messageTemplate, final TriggeringEvent trigger)
            throws FilterException {
        DeliveryPermission resultPermission = DeliveryPermission.SEND_NOW_PERMISSION;
        for (MessageDeliveryFilter deliveryFilter : deliveryFilters) {
            if (resultPermission.getVerdict() == DeliveryPermission.Verdict.SEND_NEVER)
                break;
            DeliveryPermission curFilterPermission = deliveryFilter.apply(messageTemplate, trigger);
            resultPermission = curFilterPermission.getVerdict().ordinal() > resultPermission.getVerdict().ordinal() ?
                    curFilterPermission : resultPermission;
        }

        // At this point, we should have the most restrictive permissions as calculated by filter
        return resultPermission;
    }

}
