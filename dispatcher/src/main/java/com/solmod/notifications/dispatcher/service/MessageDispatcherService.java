package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.admin.service.NotificationAccessService;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.filter.FilterException;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.repository.domain.MessageMetadata;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.solmod.notifications.dispatcher.domain.SolMessage.log;
import static com.solmod.notifications.dispatcher.domain.SolMessage.objectMapper;

@Service
public class MessageDispatcherService {
    private final MessageFilterService messageFilterService;
    private final NotificationAccessService accessService;

    @Autowired
    public MessageDispatcherService(MessageFilterService messageFilterService, NotificationAccessService accessService) {
        this.messageFilterService = messageFilterService;
        this.accessService = accessService;
    }

    /**
     *
     * @param trigger {@link TriggeringEvent}
     * @return
     */
    public String dispatchDelivery(TriggeringEvent trigger) {
        MessageTemplateGroupDTO templateGroup = accessService.getNotificationTemplateGroup(trigger.getTenantId(),
                trigger.getSubject(), trigger.getVerb());

        // TODO: If the above returned no results, then an error should be logged suggesting adjusting subscription
        TriggeredMessageTemplateGroup messagesToSend = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> dispatchTemplates = templateGroup.getMessageTemplates().stream().map(
                t -> objectMapper.convertValue(t, MessageTemplate.class)).collect(Collectors.toSet());
        // TODO: some sort of initial filtering here, perhaps based on template status?
        messagesToSend.setQualifiedTemplates(dispatchTemplates); // Before filters, all templates qualify

        for (MessageTemplate curTemplate : messagesToSend.getQualifiedTemplates()) {
            try {
                Map<Long, DeliveryPermission> deliveryPermissions = messageFilterService.determineDeliveryPermissions(messagesToSend, trigger);
                DeliveryPermission deliveryPermission = deliveryPermissions.get(curTemplate.getMessageTemplateID());
                // If the current verdict is SEND_NEVER, no sense in doing stuff anymore
                if (deliveryPermission.getVerdict() == DeliveryPermission.Verdict.SEND_NEVER) {
                    log.info("Skipping {} due to {}", curTemplate.getMessageTemplateID(), deliveryPermission.getMessage());
                    continue;
                }

                MessageDelivery messageDelivery = buildMessageDelivery(trigger, curTemplate, deliveryPermission);

            } catch (FilterException e) {
                e.printStackTrace();
            }
        }
        return "hi";
    }

    private MessageDelivery buildMessageDelivery(TriggeringEvent trigger,
                                                 MessageTemplate curTemplate,
                                                 DeliveryPermission deliveryPermission) {
        String recipientAddress = trigger.getEventMetadata().get(curTemplate.getRecipientAddressContextKey());

        MessageDelivery messageDelivery = new MessageDelivery();
        messageDelivery.setStatus(deliveryPermission.getVerdict() == DeliveryPermission.Verdict.SEND_LATER ?
                MessageDelivery.Status.PT : MessageDelivery.Status.PD);
        HashSet<MessageMetadata> messageMetadata = buildDeliveryMetadata(curTemplate, trigger);

        messageDelivery.setMessageMetadata(messageMetadata);
        messageDelivery.setRecipientAddress(recipientAddress);
        messageDelivery.setMessageTemplateId(curTemplate.getMessageTemplateID());
        messageDelivery.setSender(curTemplate.getSender());
        messageDelivery.setStatusMessage(deliveryPermission.getMessage());
        messageDelivery.setDateCreated(new Date()); // TODO: This should be done at DB layer
        return messageDelivery;
    }

    private HashSet<MessageMetadata> buildDeliveryMetadata(MessageTemplate template, TriggeringEvent trigger) {
        // Determine: What metadata is needed?
        // Depending on content block, metadata may be needed therein
        // Recipient addy
        // Message identifier key and value
        return null;
    }
}
