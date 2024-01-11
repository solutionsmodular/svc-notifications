package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class UserPreferencesFilter  implements MessageDeliveryFilter {
    @Override
    public void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        throw new NotImplementedException("UserPreferencesFilter is not yet implemented");
    }
}
