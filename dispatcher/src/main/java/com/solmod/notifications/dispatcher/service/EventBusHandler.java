package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.admin.service.NotificationAccessService;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import com.solmod.notifications.dispatcher.domain.SolCommunication;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    NotificationAccessService accessService;

    @Autowired
    public EventBusHandler(NotificationAccessService accessService) {
        this.accessService = accessService;
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
    public List<SolCommunication> apply(SolMessage solMessage) {

        MessageTemplateGroupDTO templates = accessService.getNotificationTemplateGroup(solMessage.getTenantId(), solMessage.getSubject(), solMessage.getVerb());
        // TODO: run those through the filters

        // Find the appropriate event for the given message subject and verb
        /*
        In:
            Message metadata -> Context
            Resolved recipient
            Sender
            Rendered message body (Content Manager + metadata)

        1. Get qualifying message templates (based on subject/verb)
            Create NotificationDelivery for each found

        TODO: Add Notification Group context builder here
        TODO: Add MessageTheme context builder here
        2. Apply MessageTheme metadata:criteria filter
            Update NotificationDelivery for each reject
        3. Apply MessageTheme delivery rules filter
            Update NotificationDelivery for each reject
        TODO: Add MessageTemplate context builder here
        4. Apply MessageTemplate delivery rules filter
            Update NotificationDelivery for each reject
        5. Apply UserPreferences filter
            Update NotificationDelivery for each reject
        6. Unfiltered/remaining/qualifying SolMessages...
         */
/*
                trigger = logNotificationTrigger(notificationEvent);
                Map<String, Object> context = flatten(Map.of("solmod_evt", solMessage));
                // TODO: Plus we have to get the context that's already been persisted from previous matching triggers
                Map<String, String> relevantContext = persistRelevantContext(trigger, context, messageConfigs);
*/


        return null;
    }

}


