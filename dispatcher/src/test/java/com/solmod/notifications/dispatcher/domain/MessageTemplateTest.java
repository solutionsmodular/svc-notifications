package com.solmod.notifications.dispatcher.domain;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;
import java.util.List;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTemplateTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("meetsSendRules - True when template has no send rules - null and zero")
    void assertTrueOnNoRules(int ruleVal) {

        // Arrange
        MessageTemplate template = new MessageTemplate();
        if (ruleVal == 0) {
            template.setResendInterval(ruleVal);
            template.setMaxSend(ruleVal);
        }
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(new Date());

        // Act
        DeliveryPermission result = template.applySendRules(List.of(delivery));

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("meetsSendRules - True when there are delivery rules that are met by sent deliveries")
    void assertTrueOnMaxSendAndResendIntervalMet_ByDeliveryCreatedDate() {
        // Arrange
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(10);
        template.setMaxSend(5);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -15));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));

        // Act
        DeliveryPermission result = template.applySendRules(List.of(delivery, delivery2));

        // Assert
        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("meetsSendRules - False after the max number of sends has been reached")
    void assertFalseOnMaxSendViolation() {
        // Arrange
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(20);
        template.setMaxSend(2);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -15));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));

        // Act
        DeliveryPermission result = template.applySendRules(List.of(delivery, delivery2));

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("received the max duplicates"));
    }

    @Test
    @DisplayName("meetsSendRules - False if an attempt to deliver before resend interval has been reached")
    void assertFalseOnResendIntervalViolation() {
        // Arrange
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(5);
        template.setMaxSend(3);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -3));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));

        // Act
        DeliveryPermission result = template.applySendRules(List.of(delivery, delivery2));

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("received this message within the resendInterval"));
    }

}