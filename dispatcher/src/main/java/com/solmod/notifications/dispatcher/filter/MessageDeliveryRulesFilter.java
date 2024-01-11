package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class MessageDeliveryRulesFilter implements MessageDeliveryFilter {

    MessageDeliveryRepo messageDeliveryRepo;

    public MessageDeliveryRulesFilter(MessageDeliveryRepo messageDeliveryRepo) {
        this.messageDeliveryRepo = messageDeliveryRepo;
    }

    @Override
    public void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        Set<MessageTemplate> qualifyingTemplates = templateGroup.getQualifiedTemplates();
        Iterator<MessageTemplate> templateIter = qualifyingTemplates.iterator();
        while (templateIter.hasNext()) {
            MessageTemplate curTemplate = templateIter.next();

            // Filter only if there are send rules in place
            if (curTemplate.hasSendRules()) {
                List<MessageDelivery> allDeliveries = messageDeliveryRepo.findAllDeliveries(
                        curTemplate.getMessageTemplateID(),
                        solMessage.getIdMetadataKey(),
                        solMessage.getIdMetadataValue());

                if (!curTemplate.meetsSendRules(allDeliveries)) {
                    templateIter.remove();
                }
            }
        }
    }
}
