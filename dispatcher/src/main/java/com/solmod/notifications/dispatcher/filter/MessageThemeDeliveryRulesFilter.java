package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.NotImplementedException;

public class MessageThemeDeliveryRulesFilter  implements MessageDeliveryFilter {
    @Override
    public TriggeredMessageTemplateGroup apply(TriggeredMessageTemplateGroup templateGroup) {
        throw new NotImplementedException("MessageThemeDeliveryRulesFilter is not yet implemented");
    }
}
