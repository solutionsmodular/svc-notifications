package com.solmod.notifications.admin.web.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class MessageTemplateGroupDTO implements Serializable {

    // Collection of MessageTemplates is keyed on rule-set
    private Set<MessageTemplateDTO> messageTemplates;
}
