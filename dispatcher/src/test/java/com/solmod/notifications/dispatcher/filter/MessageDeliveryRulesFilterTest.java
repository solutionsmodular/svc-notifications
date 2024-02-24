package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Date;
import java.util.List;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        SolMessage solMessage = new SolMessage();
        solMessage.setSubjectIdMetadataKey("some.key");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        DeliveryPermission result = filter.apply(template, solMessage.toTrigger());

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
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

        SolMessage solMessage = new SolMessage();
        solMessage.setSubjectIdMetadataKey("some.key");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        DeliveryPermission result = filter.apply(template, solMessage.toTrigger());

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("apply - Assert too many deliveries results in SEND_NEVER")
    void assertNonQualifyingByNumDeliveries() {
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

        SolMessage solMessage = new SolMessage();
        solMessage.setSubjectIdMetadataKey("some.key");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        DeliveryPermission result = filter.apply(template, solMessage.toTrigger());

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("received the max duplicates"));
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

        SolMessage solMessage = new SolMessage();
        solMessage.setSubjectIdMetadataKey("some.key");
        solMessage.setData(new TestDataObject("someone@somewhere.com", new TestDataChildObject("somevalue")));
        solMessage.setTenantId(15L);

        // Act
        DeliveryPermission result = filter.apply(template, solMessage.toTrigger());

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
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
    private static class TestDataObject {
        private String emailAddy;
        private TestDataChildObject some;
    }

    @Data
    @AllArgsConstructor
    private static class TestDataChildObject {
        private String key;
    }
}