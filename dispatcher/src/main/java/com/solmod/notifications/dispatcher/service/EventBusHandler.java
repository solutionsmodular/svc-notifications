package com.solmod.notifications.dispatcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solmod.notifications.admin.service.NotificationAccessService;
import com.solmod.notifications.dispatcher.domain.SolCommunication;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * This service receives and handles messages which qualify for an externally configured pub/sub queue subscription.
 * To minimize on use, this handler's subscription should be for any subjects/verbs for which there could possibly
 * be a notification.
 * // TODO: add automation to alter the queue subscription when a hitherto unknown subject/verb is added to
 * // TODO: NotificationGroups, subscribing the dispatcher to the new subject/verb combo
 */
@Service("NotificationDispatcher")
public class EventBusHandler implements Function<SolMessage, List<SolCommunication>> {

    Logger log = LoggerFactory.getLogger(getClass());

    private final NotificationAccessService accessService;
    private final MessageFilterService messageFilterService;
    private final MessageDispatcherService messageDispatcherService;
    ObjectMapper objectMapper;

    @Autowired
    public EventBusHandler(NotificationAccessService accessService,
                           MessageFilterService messageFilterService,
                           MessageDispatcherService messageDispatcherService,
                           ObjectMapper objectMapper) {
        this.accessService = accessService;
        this.messageFilterService = messageFilterService;
        this.messageDispatcherService = messageDispatcherService;
        this.objectMapper = objectMapper;
    }

    /**
     * Message Bus Subscriber
     * This signature accepts messages from the message bus which may trigger a notification. Assumes no context
     * already exists for the given trigger.
     *
     * @param solMessage {@link SolMessage} event message from off the bus
     * @return List of {@link SolCommunication}s suited to send to the sender
     */
    @Override
    public List<SolCommunication> apply(final SolMessage solMessage) {

        TriggeringEvent trigger =solMessage.toTrigger();
        messageDispatcherService.dispatchDelivery(trigger);

        return null;
    }


}


