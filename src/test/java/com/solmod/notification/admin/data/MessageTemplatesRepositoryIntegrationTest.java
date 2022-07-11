package com.solmod.notification.admin.data;

import com.solmod.notification.domain.MessageContentPurpose;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 * For all other tests, see {@link MessageTemplatesRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class MessageTemplatesRepositoryIntegrationTest {

    @Autowired
    MessageTemplatesRepository adminRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws MessageTemplateAlreadyExistsException, DBRequestFailureException {
        MessageTemplate request = new MessageTemplate();
        request.setNotificationEventId(1L);
        request.setContentKey("some.summary.key");
        request.setMessageContentPurpose(MessageContentPurpose.EMAIL);
        request.setRecipientContextKey("some.recipient.context.key");
        request.setStatus(Status.ACTIVE);

        // Call
        adminRepository.create(request);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setRecipientContextKey("data.order.owner.email");
        criteria.setContentKey("ORDER_PLACED_OWNER_EMAIL");

        MessageTemplate existing = adminRepository.getMessageTemplate(criteria);
        MessageTemplate request = new MessageTemplate();
        request.setId(existing.getId());
        request.setRecipientContextKey("data.different.order.owner.email");
        request.setContentKey("   ");

        Set<DataUtils.FieldUpdate> fieldsUpdated = adminRepository.update(request);
        assertEquals(1, fieldsUpdated.size());

        MessageTemplate updated = adminRepository.getMessageTemplate(request.getId());
        // Assert intended fields are updated
        assertEquals(request.getRecipientContextKey(), updated.getRecipientContextKey());
        // Assert other fields are as they were
        assertEquals(existing.getContentKey(), updated.getContentKey());
        assertEquals(existing.getStatus(), updated.getStatus());
        assertEquals(existing.getMessageContentPurpose(), updated.getMessageContentPurpose());
        assertTrue(updated.getDeliveryCriteria().entrySet().containsAll(existing.getDeliveryCriteria().entrySet()));
    }
}