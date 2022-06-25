package com.solmod.notification.admin.data;

import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
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
 * For all other tests, see {@link NotificationAdminRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class NotificationAdminRepositoryIntegrationTest {

    @Autowired
    NotificationAdminRepository adminRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws MessageTemplateAlreadyExistsException {
        MessageTemplate request = new MessageTemplate();
        request.setTenantId(1L);
        request.setEventSubject("Something");
        request.setEventVerb("Occurred");
        request.setContentKey("some.summary.key");
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setRecipientContextKey("some.recipient.context.key");
        request.setMessageTemplateStatus(Status.ACTIVE);

        // Call
        adminRepository.create(request);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setEventSubject("ORDER");
        criteria.setEventVerb("CREATED");
        criteria.setRecipientContextKey("data.order.owner.email");
        criteria.setContentKey("ORDER_PLACED_OWNER_EMAIL");

        MessageTemplate existing = adminRepository.getMessageTemplate(criteria);
        MessageTemplate request = new MessageTemplate();
        request.setId(existing.getId());
        request.setRecipientContextKey("data.different.order.owner.email");
        request.setContentKey("ANOTHER_CONTENT_KEY");
        request.setEventSubject("  "); // Empty field is not interpreted as a valid change and must be ignored

        Set<DataUtils.FieldUpdate> fieldsUpdated = adminRepository.update(request);
        assertEquals(2, fieldsUpdated.size());

        MessageTemplate updated = adminRepository.getMessageTemplate(request.getId());
        // Assert intended fields are updated
        assertEquals(request.getRecipientContextKey(), updated.getRecipientContextKey());
        assertEquals(request.getContentKey(), updated.getContentKey());
        // Assert other fields are changed
        assertEquals(existing.getEventSubject(), updated.getEventSubject());
        assertEquals(existing.getEventVerb(), updated.getEventVerb());
        assertEquals(existing.getMessageTemplateStatus(), updated.getStatus());
        assertEquals(existing.getContentLookupType(), updated.getContentLookupType());
        assertTrue(updated.getDeliveryCriteria().entrySet().containsAll(updated.getDeliveryCriteria().entrySet()));
    }
}