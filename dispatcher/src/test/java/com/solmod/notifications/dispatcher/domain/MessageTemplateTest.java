package com.solmod.notifications.dispatcher.domain;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        boolean result = template.meetsSendRules(List.of(delivery), new SolMessage());

        // Assert
        assertTrue(result);
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
        boolean result = template.meetsSendRules(List.of(delivery, delivery2), new SolMessage());

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("meetsSendRules - False after the max number of sends has been reached")
    @ExtendWith(OutputCaptureExtension.class)
    void assertFalseOnMaxSendViolation(CapturedOutput output) {
        // Arrange
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(20);
        template.setMaxSend(2);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -15));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));

        // Act
        boolean result = template.meetsSendRules(List.of(delivery, delivery2), new SolMessage());

        // Assert
        assertFalse(result);
        assertTrue(output.getOut().contains("would exceed maxSend rules for the template"));
    }

    @Test
    @DisplayName("meetsSendRules - False if an attempt to deliver before resend interval has been reached")
    @ExtendWith(OutputCaptureExtension.class)
    void assertFalseOnResendIntervalViolation(CapturedOutput output) {
        // Arrange
        MessageTemplate template = new MessageTemplate();
        template.setResendInterval(5);
        template.setMaxSend(3);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -3));
        MessageDelivery delivery2 = new MessageDelivery();
        delivery2.setDateCreated(DateUtils.addMinutes(new Date(), -20));

        // Act
        boolean result = template.meetsSendRules(List.of(delivery, delivery2), new SolMessage());

        // Assert
        assertFalse(result);
        assertTrue(output.getOut().contains("would violate resend interval"));
    }

}