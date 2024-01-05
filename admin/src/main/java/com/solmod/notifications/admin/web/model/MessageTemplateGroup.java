package com.solmod.notifications.admin.web.model;

import com.solmod.notifications.admin.repository.model.MessageTemplate;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class MessageTemplateGroup implements Serializable {

    // Collection of MessageTemplates is keyed on rule-set
    private Map<TemplateRuleset, Set<MessageTemplate>> messageTemplates;
}
