package com.solmod.notification.admin.data;

import com.solmod.notification.domain.MessageConfig;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 * For all other tests, see {@link MessageTemplatesRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class MessageConfigsRepositoryIntegrationTest {

    @Autowired
    MessageConfigsRepository adminRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws DataCollisionException, DBRequestFailureException {
        // Get the test message config to get its notification event id
        MessageConfig model = new MessageConfig();
        model.setName("notification-admin-test-mc");
        model = adminRepository.getMessageConfig(model);

        MessageConfig request = new MessageConfig();
        request.setNotificationEventId(model.getNotificationEventId());
        request.setStatus(Status.ACTIVE);
        request.setName("new_message-config-test-config");

        // Call
        Long newId = adminRepository.create(request);

        assertNotNull(newId);
        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws ExpectedNotFoundException, DataCollisionException {
        MessageConfig model = new MessageConfig();
        model.setName("notification-admin-test-mc");
        model = adminRepository.getMessageConfig(model);

        MessageConfig request = new MessageConfig();
        request.setId(model.getId());
        request.setName("some-different-name");
        request.setStatus(Status.INACTIVE);

        Set<DataUtils.FieldUpdate> fieldsUpdated = adminRepository.update(request);
        assertEquals(2, fieldsUpdated.size());

        MessageConfig updated = adminRepository.getMessageConfig(request.getId());
        assertEquals(request.getName(), updated.getName());
        assertEquals(request.getStatus(), updated.getStatus());
    }
}