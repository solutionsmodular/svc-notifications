package com.solmod.notifications.dispatcher.service.domain;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class TriggeredMessageTemplateGroup {

    private Set<MessageTemplate> qualifiedTemplates;
    private Map<Long, String> denyMessages = new HashMap<>();

    /**
     * During filter, any nonqualifying templates will be removed from qualifiedTemplates.
     * A deny message is all that will remain of record of that template
     *
     * @param messageTemplateId {@code Long} identifying the message template from the admin services
     * @param denyMessage String human-readable description of the reason for the rejection
     */
    public void addDenyMessage(Long messageTemplateId, String denyMessage) {
        denyMessages.put(messageTemplateId, denyMessage);
    }
}
