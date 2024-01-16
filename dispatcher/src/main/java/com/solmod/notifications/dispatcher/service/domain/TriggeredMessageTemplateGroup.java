package com.solmod.notifications.dispatcher.service.domain;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import lombok.Data;

import java.util.Set;

@Data
public class TriggeredMessageTemplateGroup {

    private Set<MessageTemplate> qualifiedTemplates;
}
