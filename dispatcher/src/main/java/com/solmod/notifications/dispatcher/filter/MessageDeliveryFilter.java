package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;

@FunctionalInterface
public interface MessageDeliveryFilter {

    /**
     * Run logic to determine send'ability of a message
     * Implementations of MessageDeliveryFilter must:
     * 1. Receive a {@link  TriggeredMessageTemplateGroup}
     * 2. Assess each MessageTemplate
     *
     * @param templateGroup {@link TriggeredMessageTemplateGroup}
     * @param trigger    {@link com.solmod.notifications.dispatcher.domain.TriggeringEvent}
     * @return {@link FilterResponse}
     * @throws FilterException in the event of an error preventing proper calculation/processing
     */
    FilterResponse apply(final TriggeredMessageTemplateGroup templateGroup, TriggeringEvent trigger) throws FilterException;
}
