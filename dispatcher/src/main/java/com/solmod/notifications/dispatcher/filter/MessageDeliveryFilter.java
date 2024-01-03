package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.filter.domain.MessageDeliveryTrigger;

public interface MessageDeliveryFilter {

    /**
     * Run logic to determine send'ability of a message
     *
     * @param trigger
     * @return
     */
    MessageDeliveryTrigger apply(MessageDeliveryTrigger trigger);
/*
    Message metadata -> Context
    Resolved recipient
    Sender
    Rendered message body (Content Manager + metadata)
*/

}
