package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;

public class MessageDeliveryRulesFilter implements MessageDeliveryFilter {
    @Override
    public void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        for (MessageTemplateDTO curTemplate : templateGroup.getQualifiedTemplates().getMessageTemplates()) {

            // 1. if template has no delivery rules
            if (curTemplate.getMaxSend() != null && !curTemplate.getMaxSend().equals(0)) {
                // do the checking
                // 2. Get all deliveries where template ID and message identifier metadata are the same

            }
        }
    }
}
