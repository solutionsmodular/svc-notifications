package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MessageDeliveryRulesFilter implements MessageDeliveryFilter {

    MessageDeliveryRepo messageDeliveryRepo;

    public MessageDeliveryRulesFilter(MessageDeliveryRepo messageDeliveryRepo) {
        this.messageDeliveryRepo = messageDeliveryRepo;
    }

    @Override
    public FilterResponse apply(final TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        FilterResponse response = new FilterResponse("delivery-rules");
        if (templateGroup.getQualifiedTemplates().isEmpty()) {
            return response;
        }

        Set<MessageTemplate> qualifyingTemplates = templateGroup.getQualifiedTemplates();
        for (MessageTemplate curTemplate : qualifyingTemplates) {
            // Filter only if there are send-rules in place
            String recipientAddress = solMessage.getMetadata().getOrDefault(
                    curTemplate.getRecipientAddressContextKey(), "").toString();
            if (curTemplate.hasSendRules()) {
                List<MessageDelivery> allDeliveries = messageDeliveryRepo.findAllDeliveries(
                        curTemplate.getMessageTemplateID(),
                        recipientAddress,
                        solMessage.getIdMetadataKey(),
                        solMessage.getIdMetadataValue());

                response.addDeliveryPermission(curTemplate.getMessageTemplateID(), curTemplate.applySendRules(allDeliveries));
            } else {
                response.addDeliveryPermission(curTemplate.getMessageTemplateID(), DeliveryPermission.SEND_NOW_PERMISSION);
            }
        }

        return response;
    }
}
