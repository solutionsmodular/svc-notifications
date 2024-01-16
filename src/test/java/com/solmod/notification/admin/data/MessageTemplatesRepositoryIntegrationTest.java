package com.solmod.notification.admin.data;

import com.solmod.notification.domain.MessageConfig;
import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DataCollisionException;
import com.solmod.notification.exception.ExpectedNotFoundException;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    MessageConfigsRepository configRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws DataCollisionException {
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setName("notification-admin-test-mc");
        messageConfig = configRepository.getMessageConfig(messageConfig);

        MessageTemplate request = new MessageTemplate();
        request.setMessageConfigId(messageConfig.getId());
        request.setContentKey("some.summary.key");
        request.setMessageSender(MessageSender.EMAIL);
        request.setRecipientContextKey("some.recipient.context.key");
        request.setStatus(Status.ACTIVE);

        // Call
        Long resultId = adminRepository.create(request);

        assertNotNull(resultId);
        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws ExpectedNotFoundException, DataCollisionException {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setRecipientContextKey("test-recipient-addy");

        List<MessageTemplate> messageTemplates = adminRepository.getMessageTemplates(criteria);
        MessageTemplate existing = messageTemplates.get(0);
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
        assertEquals(existing.getMessageSender(), updated.getMessageSender());
    }
}