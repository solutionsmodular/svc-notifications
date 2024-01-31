package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.SolMessage;
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
     * @param solMessage    {@link SolMessage}
     * @return {@link FilterResponse}
     * @throws {@link FilterException} in the event of an error preventing proper calculation/processing
     */
    FilterResponse apply(final TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage)
            throws FilterException;
}
