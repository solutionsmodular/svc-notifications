package com.solmod.notification.admin.data;

import com.solmod.notification.engine.domain.MessageConfig;
import com.solmod.notification.engine.domain.NotificationDelivery;
import com.solmod.notification.engine.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class NotificationDeliveriesRepositoryIntegrationTest {

    @Autowired
    MessageTemplatesRepository messageTemplatesRepo;

    @Autowired
    NotificationDeliveriesRepository deliveriesRepo;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only. All optional fields omitted")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate_onlyReqFields(CapturedOutput output) throws DBRequestFailureException {

        MessageConfig template = getLiveMessageTemplate();

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setRecipient("some-recipient-identifier");
        delivery.setMessageTemplateId(template.getId());
        delivery.setStatus(Status.PENDING_DELIVERY);

        // Call
        deliveriesRepo.create(delivery);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));

        NotificationDelivery criteria = new NotificationDelivery();
        criteria.setRecipient(delivery.getRecipient());
        List<NotificationDelivery> notificationDeliveries = deliveriesRepo.getNotificationDeliveries(criteria);
        assertEquals(1, notificationDeliveries.size());

        NotificationDelivery result = notificationDeliveries.get(0);
        assertNull(result.getMessageBodyUri());
        assertNull(result.getDeliveryProcessKey());
        assertEquals(delivery.getRecipient(), result.getRecipient());
        assertEquals(delivery.getMessageTemplateId(), result.getMessageTemplateId());
        assertEquals(delivery.getStatus(), result.getStatus());
    }
    @Test
    @DisplayName("Testing create. Happy day case in integration test, only. All optional fields included")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate_allFields(CapturedOutput output) throws DBRequestFailureException {

        MessageConfig template = getLiveMessageTemplate();

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setRecipient("some-recipient-identifier");
        delivery.setMessageTemplateId(template.getId());
        delivery.setStatus(Status.PENDING_DELIVERY);
        delivery.setDeliveryProcessKey("some-delivery-key");
        delivery.setMessageBodyUri("some-mburi");

        // Call
        deliveriesRepo.create(delivery);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));

        NotificationDelivery criteria = new NotificationDelivery();
        criteria.setRecipient(delivery.getRecipient());
        List<NotificationDelivery> notificationDeliveries = deliveriesRepo.getNotificationDeliveries(criteria);
        assertEquals(1, notificationDeliveries.size());

        NotificationDelivery result = notificationDeliveries.get(0);
        assertEquals(delivery.getMessageBodyUri(), result.getMessageBodyUri());
        assertEquals(delivery.getDeliveryProcessKey(), result.getDeliveryProcessKey());
        assertEquals(delivery.getRecipient(), result.getRecipient());
        assertEquals(delivery.getMessageTemplateId(), result.getMessageTemplateId());
        assertEquals(delivery.getStatus(), result.getStatus());
    }

    private MessageConfig getLiveMessageTemplate() {
        MessageConfig criteria = new MessageConfig();
        criteria.setContentKey("ORDER_PLACED_OWNER_EMAIL");
        return messageTemplatesRepo.getMessageTemplates(criteria).get(0);
    }
}