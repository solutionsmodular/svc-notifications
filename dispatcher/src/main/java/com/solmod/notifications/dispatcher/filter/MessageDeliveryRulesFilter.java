package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageDeliveryRulesFilter implements MessageDeliveryFilter {

    MessageDeliveryRepo messageDeliveryRepo;

    public MessageDeliveryRulesFilter(MessageDeliveryRepo messageDeliveryRepo) {
        this.messageDeliveryRepo = messageDeliveryRepo;
    }

    @Override
    public String getFilterName() {
        return "delivery-rules";
    }

    @Override
    public DeliveryPermission apply(final MessageTemplate messageTemplate, TriggeringEvent trigger) {

        String recipientAddress = trigger.getEventMetadata().getOrDefault(
                messageTemplate.getRecipientAddressContextKey(), "");
        if (messageTemplate.hasSendRules()) {
            List<MessageDelivery> allDeliveries = messageDeliveryRepo.findAllDeliveries(
                    messageTemplate.getMessageTemplateID(),
                    recipientAddress,
                    trigger.getSubjectIdMetadataKey(),
                    trigger.getEventMetadata().get(trigger.getSubjectIdMetadataKey()));

            return messageTemplate.applySendRules(allDeliveries);
        } else {
            return DeliveryPermission.SEND_NOW_PERMISSION;
        }
    }
}
