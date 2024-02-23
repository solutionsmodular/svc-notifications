package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.filter.FilterException;
import com.solmod.notifications.dispatcher.filter.FilterResponse;
import com.solmod.notifications.dispatcher.filter.MessageDeliveryFilter;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class MessageFilterService {

    private final Set<MessageDeliveryFilter> deliveryFilters;

    @Autowired
    public MessageFilterService(Set<MessageDeliveryFilter> deliveryFilters) {
        this.deliveryFilters = deliveryFilters;
    }

    /**
     * This method WILL modify the contents of the provided {@link TriggeredMessageTemplateGroup} param.
     * If original {@link TriggeredMessageTemplateGroup} is required alongside filtered, clone before calling
     *
     * @param templateGroup {@link TriggeredMessageTemplateGroup}
     * @param trigger    {@link TriggeringEvent}
     * @return Map of {@link DeliveryPermission} representing the most restrictive of the verdicts as calculated by each filter
     */
    public Map<Long, DeliveryPermission> applyDeliveryFilters(TriggeredMessageTemplateGroup templateGroup, final TriggeringEvent trigger)
            throws FilterException {
        Map<Long, DeliveryPermission> resultingPermissions = new HashMap<>();

        for (MessageDeliveryFilter deliveryFilter : deliveryFilters) {
            // TODO stop processing filters for any template that's SEND_NEVER
            FilterResponse groupPermissions = deliveryFilter.apply(templateGroup, trigger);
            overlayTemplatePermission(groupPermissions, resultingPermissions);
        }

        // At this point, we should have a Map of the most restrictive permissions as calculated by filter
        return resultingPermissions;
    }

    /**
     * Adjust or add to the resulting permissions the least permitted of the verdicts (the highest ordinal = most restrictive)
     *
     * @param groupPermissions     {@link FilterResponse}
     * @param resultingPermissions {@code Map} of {@link DeliveryPermission}s, changed by this method
     */
    void overlayTemplatePermission(FilterResponse groupPermissions, Map<Long, DeliveryPermission> resultingPermissions) {
        for (Map.Entry<Long, DeliveryPermission> curGroupPermission : groupPermissions.getPermissions().entrySet()) {
            Long templateId = curGroupPermission.getKey();
            DeliveryPermission startingDeliveryPermission =
                    Objects.requireNonNullElse(resultingPermissions.get(templateId), DeliveryPermission.SEND_NOW_PERMISSION);
            DeliveryPermission overlayPermission = curGroupPermission.getValue();

            resultingPermissions.put(templateId,
                    overlayPermission.getVerdict().ordinal() > startingDeliveryPermission.getVerdict().ordinal() ?
                        overlayPermission : startingDeliveryPermission);
        }
    }
}
