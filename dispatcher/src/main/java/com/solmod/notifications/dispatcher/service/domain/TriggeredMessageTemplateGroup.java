package com.solmod.notifications.dispatcher.service.domain;

import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TriggeredMessageTemplateGroup {

    private MessageTemplateGroupDTO qualifiedTemplates;
    private List<String> denyMessages;

    /**
     * Post filter, a nonqualifying template will be removed from qualifiedTemplates.
     * A deny message is all that will remain of record of that template
     *
     * @param denyMessage String
     */
    public void addDenyMessage(String denyMessage) {
        if (denyMessages == null) {
            denyMessages = new LinkedList<>();
        }

        denyMessages.add(denyMessage);
    }
}
