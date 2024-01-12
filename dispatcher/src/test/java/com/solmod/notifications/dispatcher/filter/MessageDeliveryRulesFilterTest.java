package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class MessageDeliveryRulesFilterTest {

    @InjectMocks
    MessageDeliveryRulesFilter filter;
    @Mock
    MessageDeliveryRepo repo;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @Test
    @DisplayName("findAllDeliveries - Assert qualifying templates are not removed by filter")
    void assertQualifyingTemplatesNotRemoved() {
        // Arrange
        MessageTemplate template = buildTemplate();

        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -15));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setTenantId(15L);

        // Act
        filter.apply(triggeredGroup, solMessage);

        // Assert
        assertEquals(1, triggeredGroup.getQualifiedTemplates().size());
    }

    @Test
    @DisplayName("findAllDeliveries - Assert templates are removed by filter")
    void assertNonQualifyingTemplatesRemoved() {
        // Arrange
        MessageTemplate template = buildTemplate();

        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -1));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -6));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setTenantId(15L);

        // Act
        filter.apply(triggeredGroup, solMessage);

        // Assert
        assertTrue(triggeredGroup.getQualifiedTemplates().isEmpty());
    }

    @NotNull
    private MessageTemplate buildTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(15);
        template.setMaxSend(5);
        return template;
    }

}