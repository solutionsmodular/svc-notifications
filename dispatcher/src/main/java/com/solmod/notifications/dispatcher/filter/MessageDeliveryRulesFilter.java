package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.stereotype.Component;

@Component
public class MessageDeliveryRulesFilter implements MessageDeliveryFilter {

    MessageDeliveryRepo messageDeliveryRepo;

    public MessageDeliveryRulesFilter(MessageDeliveryRepo messageDeliveryRepo) {
        this.messageDeliveryRepo = messageDeliveryRepo;
    }

    @Override
    public void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        for (MessageTemplateDTO curTemplate : templateGroup.getQualifiedTemplates().getMessageTemplates()) {

            // 1. if template has no delivery rules
            if (curTemplate.getMaxSend() != null && !curTemplate.getMaxSend().equals(0)
                    && curTemplate.getResendInterval() != null && !curTemplate.getResendInterval().equals(0)) {
                // do the checking
                // 2. Get all deliveries where template ID and message identifier metadata are the same
                // 2.a How long is the wait?
                curTemplate.getMaxSend(); // 0 means no max
                curTemplate.getResendInterval(); // in minutes, 0 means no wait
                messageDeliveryRepo.findAllDeliveries(
                        curTemplate.getMessageTemplateID(),
                        solMessage.getIdMetadataKey(),
                        solMessage.getIdMetadataValue());
            }
        }
    }
}
