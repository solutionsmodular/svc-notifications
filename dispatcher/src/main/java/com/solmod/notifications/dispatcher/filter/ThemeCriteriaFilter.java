package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class ThemeCriteriaFilter  implements MessageDeliveryFilter {
    @Override
    public TriggeredMessageTemplateGroup apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        throw new NotImplementedException("MessageTemplateDeliveryRulesFilter is not yet implemented");
    }
}
