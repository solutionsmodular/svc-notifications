package com.solmod.notification.admin.data;

import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.MessageTemplateStatus;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 * For all other tests, see {@link NotificationAdminRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
class NotificationAdminRepositoryIntegrationTest {

    @Autowired
    NotificationAdminRepository adminRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    void testCreate() throws MessageTemplateAlreadyExistsException {
        MessageTemplate request = new MessageTemplate();
        request.setTenantId(1L);
        request.setEventSubject("Something");
        request.setEventVerb("Occurred");
        request.setContentKey("some.summary.key");
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setRecipientContextKey("some.recipient.context.key");
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);

        // Call
        MessageTemplate savedTemplate = adminRepository.create(request);

        // Assert
        System.out.println("Saved new Message Template, id: " + savedTemplate.getId());
        assertEquals(request.getTenantId(), savedTemplate.getTenantId());
        assertEquals(request.getEventSubject(), savedTemplate.getEventSubject());
        assertEquals(request.getEventVerb(), savedTemplate.getEventVerb());
        assertEquals(request.getContentKey(), savedTemplate.getContentKey());
        assertEquals(request.getContentLookupType(), savedTemplate.getContentLookupType());
        assertEquals(request.getRecipientContextKey(), savedTemplate.getRecipientContextKey());
        assertEquals(request.getMessageTemplateStatus(), savedTemplate.getMessageTemplateStatus());
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate request = adminRepository.getMessageTemplate(new UniqueMessageTemplateId(1L, "ORDER", "CREATED", "data.order.owner.email", "ORDER_PLACED_OWNER_EMAIL"));
        request.setRecipientContextKey("data.different.order.owner.email");
        request.setContentKey("ANOTHER_CONTENT_KEY");

        Set<DataUtils.FieldUpdate> updated = adminRepository.update(request);
        assertEquals(2, updated.size());

        MessageTemplate messageTemplate = adminRepository.getMessageTemplate(request.getId());
        assertEquals(request.getRecipientContextKey(), messageTemplate.getRecipientContextKey());
        assertEquals(request.getContentKey(), messageTemplate.getContentKey());
    }
}