package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.NotImplementedException;

public class ThemeCriteriaFilter  implements MessageDeliveryFilter {
    @Override
    public TriggeredMessageTemplateGroup apply(TriggeredMessageTemplateGroup templateGroup) {
        throw new NotImplementedException("MessageTemplateDeliveryRulesFilter is not yet implemented");
    }
}
