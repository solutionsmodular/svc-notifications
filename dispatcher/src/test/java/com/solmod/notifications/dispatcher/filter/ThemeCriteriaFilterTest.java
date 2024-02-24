package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

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

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);

        DeliveryPermission result = filter.apply(messageTemplate, solMessage.toTrigger());

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("Template with criteria qualifies with qualifying metadata")
    void qualifyingTemplate_WithCriteria() {
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2"));
        messageTemplate.setDeliveryCriteria(criteriaSet);

        DeliveryPermission result = filter.apply(messageTemplate, solMessage.toTrigger());

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("Templates with criteria result in removal for messages without required criteria")
    void nonQualifyingTemplate_MissingRequired() {
        // Arrange
        ThemeCriteriaFilter filter = new ThemeCriteriaFilter();
        SolMessage solMessage = new SolMessage();
        Map<String, Object> mockData = Map.of("key1", "val1", "key2", "val2");
        solMessage.setData(mockData);

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2", "key3", "val3"));
        messageTemplate.setDeliveryCriteria(criteriaSet);

        // Act
        DeliveryPermission result = filter.apply(messageTemplate, solMessage.toTrigger());

        // Assert
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

        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setMessageTemplateID(88L);
        DeliveryCriterionSetDTO criteriaSet = new DeliveryCriterionSetDTO();
        criteriaSet.setCriteria(Map.of("key1", "val1", "key2", "val2"));
        messageTemplate.setDeliveryCriteria(criteriaSet);

        // Act
        DeliveryPermission result = filter.apply(messageTemplate, solMessage.toTrigger());

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("incorrect value for template criterion key2"));
    }

}
