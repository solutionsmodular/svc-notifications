package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.filter.domain.MessageDeliveryTrigger;
import org.apache.commons.lang3.NotImplementedException;

public class MessageThemeDeliveryRulesFilter  implements MessageDeliveryFilter {
    @Override
    public MessageDeliveryTrigger apply(MessageDeliveryTrigger trigger) {
        throw new NotImplementedException("MessageThemeDeliveryRulesFilter is not yet implemented");
    }
}
