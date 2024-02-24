package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;

public interface MessageDeliveryFilter {

    /**
     * Run logic to determine send'ability of a message
     * Implementations of MessageDeliveryFilter must:
     *
     * @param messageTemplate {@link MessageTemplate}
     * @param trigger    {@link com.solmod.notifications.dispatcher.domain.TriggeringEvent}
     * @return {@link DeliveryPermission}
     * @throws FilterException in the event of an error preventing proper calculation/processing
     */
    DeliveryPermission apply(final MessageTemplate messageTemplate, TriggeringEvent trigger) throws FilterException;

    String getFilterName();
}
