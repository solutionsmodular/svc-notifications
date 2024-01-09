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
     * @param templateGroup
     * @param solMessage
     * @return
     */
    void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage);
/*
    Message metadata -> Context
    Resolved recipient
    Sender
    Rendered message body (Content Manager + metadata)
*/

}
