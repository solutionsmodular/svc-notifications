package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.filter.MessageDeliveryFilter;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
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
     * This method WILL modify the contents of the provided {@link TriggeredMessageTemplateGroup} param.
     * If original {@link TriggeredMessageTemplateGroup} is required alongside filtered, clone before calling
     *
     * @param templateGroup {@link TriggeredMessageTemplateGroup}
     * @param solMessage {@link SolMessage}
     */
    public void runThroughFilters(TriggeredMessageTemplateGroup templateGroup, final SolMessage solMessage) {
        for (MessageDeliveryFilter deliveryFilter : deliveryFilters) {
            deliveryFilter.apply(templateGroup, solMessage);
        }
    }
}
