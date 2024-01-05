package com.solmod.notifications.dispatcher.service.domain;

import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import com.solmod.notifications.dispatcher.filter.domain.MessageDeliveryTrigger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TriggeredMessageTemplateGroup {

    private MessageTemplateGroupDTO messageTemplateGroup;
    private MessageDeliveryTrigger deliveryTrigger;
}
