package com.solmod.notifications.dispatcher.service;

import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.filter.FilterException;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
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

import static com.solmod.notifications.dispatcher.domain.SolMessage.objectMapper;

@Service
public class MessageDispatcherService {
    private final MessageDeliveryRepo repo;
    private final MessageFilterService messageFilterService;

    @Autowired
    public MessageDispatcherService(MessageDeliveryRepo repo, MessageFilterService messageFilterService) {
        this.repo = repo;
        this.messageFilterService = messageFilterService;
    }

    public String dispatchDelivery(MessageTemplateGroupDTO templateGroup,
                                   SolMessage solMessage) {
        // TODO: If the above returned no results, then an error should be logged suggesting adjusting subscription
        TriggeredMessageTemplateGroup messagesToSend = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> dispatchTemplates = templateGroup.getMessageTemplates().stream().map(
                t -> objectMapper.convertValue(t, MessageTemplate.class)).collect(Collectors.toSet());
        // TODO: some sort of initial filtering here, perhaps based on template status?
        messagesToSend.setQualifiedTemplates(dispatchTemplates); // Before filters, all templates qualify

        for (MessageTemplate curTemplate : messagesToSend.getQualifiedTemplates()) {
            /*
            If permission.send_never, skip
            If permission.send_later, create delivery for delayed send
            If permission.send_now, create delivery for send now
             */

            try {
                Map<Long, DeliveryPermission> deliveryPermissions = messageFilterService.determineDeliveryPermissions(messagesToSend, solMessage);
                DeliveryPermission deliveryPermission = deliveryPermissions.get(curTemplate.getMessageTemplateID());
                if (deliveryPermission.getVerdict() == DeliveryPermission.Verdict.SEND_NEVER) {
                    continue;
                }

                MessageDelivery messageDelivery = buildMessageDelivery(solMessage, curTemplate, deliveryPermission);
            } catch (FilterException e) {
                e.printStackTrace();
            }
        }
        return "hi";
    }

    private MessageDelivery buildMessageDelivery(SolMessage solMessage,
                                                 MessageTemplate curTemplate,
                                                 DeliveryPermission deliveryPermission) {
        String recipientAddress = solMessage.getMetadata(curTemplate.getRecipientAddressContextKey()).toString();

        MessageDelivery messageDelivery = new MessageDelivery();
        messageDelivery.setStatus(deliveryPermission.getVerdict() == DeliveryPermission.Verdict.SEND_LATER ?
                MessageDelivery.Status.PT : MessageDelivery.Status.PD);
        HashSet<MessageMetadata> messageMetadata = buildDeliveryMetadata(curTemplate, solMessage);

        messageDelivery.setMessageMetadata(messageMetadata);
        messageDelivery.setRecipientAddress(recipientAddress);
        messageDelivery.setMessageTemplateId(curTemplate.getMessageTemplateID());
        messageDelivery.setSender(curTemplate.getSender());
        messageDelivery.setStatusMessage(deliveryPermission.getMessage());
        messageDelivery.setDateCreated(new Date()); // TODO: This should be done at DB layer
        return messageDelivery;
    }

    private HashSet<MessageMetadata> buildDeliveryMetadata(MessageTemplate template, SolMessage message) {
        // Determine: What metadata is needed?
        // Depending on content block, metadata may be needed therein
        // Recipient addy
        // Message identifier key and value
        return null;
    }
}
