package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeCriteriaFilterTest {

    @Test
    @DisplayName("Template without criteria qualifies regardless of metadata")
    void qualifyingTemplate_WithoutCriteria() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        templateGroup.setQualifiedTemplates(Set.of(messageTemplate));

        FilterResponse response = filter.apply(templateGroup, solMessage);

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, response.getPermissions().get(88L));
    }

    @Test
    @DisplayName("Template with criteria qualifies with qualifying metadata")
    void qualifyingTemplate_WithCriteria() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2"));
        messageTemplate.setDeliveryCriteria(criteriaSet);
        templateGroup.setQualifiedTemplates(Set.of(messageTemplate));

        FilterResponse response = filter.apply(templateGroup, solMessage);

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, response.getPermissions().get(88L));
    }

    @Test
    @DisplayName("Templates with criteria result in removal for messages without required criteria")
    void nonQualifyingTemplate_MissingRequired() {
        // Arrange
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2", "key3", "val3"));
        messageTemplate.setDeliveryCriteria(criteriaSet);
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(messageTemplate);
        templateGroup.setQualifiedTemplates(templates);

        // Act
        FilterResponse response = filter.apply(templateGroup, solMessage);

        // Assert
        DeliveryPermission result = response.getPermissions().get(88L);
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("missing template criterion key3"));
    }

    @Test
    @DisplayName("Templates with criteria result in removal for messages without required criteria")
    void nonQualifyingTemplate_MismatchedRequired() {
        // Arrange
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val_wrong");
        solMessage.setData(mockData);

        TriggeredMessageTemplateGroup templateGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2"));
        messageTemplate.setDeliveryCriteria(criteriaSet);
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(messageTemplate);
        templateGroup.setQualifiedTemplates(templates);

        // Act
        FilterResponse response = filter.apply(templateGroup, solMessage);

        // Assert
        DeliveryPermission result = response.getPermissions().get(88L);
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("incorrect value for template criterion key2"));
    }

}
