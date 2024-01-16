package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.*;

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
    @DisplayName("apply - Assert SEND_NOW when template has no resend rules")
    void assertTemplateWithNoRules() {
        // Arrange
        MessageTemplate template = buildTemplate();
        template.setRecipientAddressContextKey("emailAddy");

        MessageDelivery delivery = new MessageDelivery();
        delivery.setRecipientAddress("someone@somewhere.com");
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -1));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -2));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "someone@somewhere.com", "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        FilterResponse response = filter.apply(triggeredGroup, solMessage);

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW, response.getPermissions().get(template.getMessageTemplateID()));
    }

    @Test
    @DisplayName("findAllDeliveries - Assert SEND_NOW when template has rules that are not violated")
    void assertTemplateWithRules_RulesMet() {
        // Arrange
        MessageTemplate template = buildTemplate();
        template.setRecipientAddressContextKey("emailAddy");
        template.setResendInterval(5);
        template.setMaxSend(5);

        MessageDelivery delivery = new MessageDelivery();
        delivery.setRecipientAddress("someone@somewhere.com");
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -10));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "someone@somewhere.com", "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        FilterResponse response = filter.apply(triggeredGroup, solMessage);

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW, response.getPermissions().get(template.getMessageTemplateID()));
    }

    @Test
    @DisplayName("apply - Assert too many deliveries results in SEND_NEVER")
    @ExtendWith(OutputCaptureExtension.class)
    void assertNonQualifyingByNumDeliveries(CapturedOutput output) {
        // Arrange
        MessageTemplate template = buildTemplate();
        template.setMaxSend(2);
        template.setRecipientAddressContextKey("emailAddy");

        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -30));
        delivery.setRecipientAddress("someone@somewhere.com");
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -60));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "someone@somewhere.com", "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        FilterResponse response = filter.apply(triggeredGroup, solMessage);

        // Assert
        assertEquals(DeliveryPermission.SEND_NEVER, response.getPermissions().get(template.getMessageTemplateID()));
        assertTrue(output.getOut().contains("would exceed maxSend rules"));
    }

    @Test
    @DisplayName("apply - Assert too recent deliveries results in SEND_NEVER")
    void assertNonQualifyingByResendTooSoon() {
        // Arrange
        MessageTemplate template = buildTemplate();

        template.setMaxSend(2);
        template.setRecipientAddressContextKey("emailAddy");

        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -10));
        delivery.setRecipientAddress("someone@somewhere.com");
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -60));
        when(repo.findAllDeliveries(template.getMessageTemplateID(), "someone@somewhere.com", "some.key", "somevalue"))
                .thenReturn(List.of(delivery, delivery2));

        TriggeredMessageTemplateGroup triggeredGroup = new TriggeredMessageTemplateGroup();
        Set<MessageTemplate> templates = new HashSet<>();
        templates.add(template);
        triggeredGroup.setQualifiedTemplates(templates);

        SolMessage solMessage = new SolMessage();
        solMessage.setIdMetadataKey("some.key");
        solMessage.setIdMetadataValue("somevalue");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        FilterResponse response = filter.apply(triggeredGroup, solMessage);

        // Assert
        assertEquals(DeliveryPermission.SEND_NEVER, response.getPermissions().get(template.getMessageTemplateID()));
    }

    @NotNull
    private MessageTemplate buildTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(87L);
        template.setRecipientAddressContextKey("data.testThing.testAttribute");
        return template;
    }


    @Data
    @AllArgsConstructor
    private class TestDataObject {
        private String emailAddy;
        private TestDataChildObject some;
    }

    @Data
    @AllArgsConstructor
    private class TestDataChildObject {
        private String key;
    }
}