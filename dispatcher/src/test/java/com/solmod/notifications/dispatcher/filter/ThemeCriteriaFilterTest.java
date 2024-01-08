package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemeCriteriaFilterTest {

    @Test
    @DisplayName("Template without criteria qualifies with qualifying metadata")
    void qualifyingTemplate_WithoutCriteria() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, String> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplateDTO messageTemplate = new MessageTemplateDTO();
        MessageTemplateGroupDTO qualifiedTemplates = new MessageTemplateGroupDTO();
        qualifiedTemplates.setMessageTemplates((Set.of(messageTemplate)));
        templateGroup.setQualifiedTemplates(qualifiedTemplates);

        filter.apply(templateGroup, solMessage);

        assertEquals(1, templateGroup.getQualifiedTemplates().getMessageTemplates().size()); // Same 1 as what was added to the group
    }

    @Test
    @DisplayName("Template with criteria qualifies with qualifying metadata")
    void qualifyingTemplate_WithCriteria() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, String> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplateDTO messageTemplate = new MessageTemplateDTO();
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2"));
        messageTemplate.setDeliveryCriteria(criteriaSet);
        MessageTemplateGroupDTO qualifiedTemplates = new MessageTemplateGroupDTO();
        qualifiedTemplates.setMessageTemplates((Set.of(messageTemplate)));
        templateGroup.setQualifiedTemplates(qualifiedTemplates);

        filter.apply(templateGroup, solMessage);

        assertEquals(1, templateGroup.getQualifiedTemplates().getMessageTemplates().size()); // Same 1 as what was added to the group
    }

    @Test
    void nonQualifyingTemplate() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, String> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplateDTO messageTemplate = new MessageTemplateDTO();
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2", "key3", "val3"));
        messageTemplate.setDeliveryCriteria(criteriaSet);
        MessageTemplateGroupDTO qualifiedTemplates = new MessageTemplateGroupDTO();
        Set<MessageTemplateDTO> templates = new HashSet<>();
        templates.add(messageTemplate);
        qualifiedTemplates.setMessageTemplates(templates);
        templateGroup.setQualifiedTemplates(qualifiedTemplates);

        filter.apply(templateGroup, solMessage);

        assertEquals(0, templateGroup.getQualifiedTemplates().getMessageTemplates().size()); // Same 1 as what was added to the group
    }

}