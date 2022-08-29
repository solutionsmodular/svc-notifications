package com.solmod.notification.admin.data;

import com.solmod.notification.domain.NotificationTrigger;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 * For all other tests, see {@link NotificationTriggersRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class NotificationTriggersContextRepositoryIntegrationTest {

    @Autowired
    NotificationTriggersRepository triggerRepo;

    @Autowired
    NotificationTriggerContextRepository triggerContextRepo;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we can save context relative to a NotificationTrigger. This test uses data from notification-admin-tests.sql")
    void testCreateCriteria(CapturedOutput output) throws DBRequestFailureException {
        NotificationTrigger criteria = new NotificationTrigger();
        criteria.setUid("existing-uid");

        List<NotificationTrigger> notificationTriggers = triggerRepo.getNotificationTriggers(criteria);

        assertEquals(1, notificationTriggers.size());

        Map<String, String> someContext = Map.of("key1", "value1", "key2", "4455", "key3", "true");

        triggerContextRepo.saveContext(notificationTriggers.get(0).getId(), someContext);

        assertTrue(output.getOut().contains("NotificationTriggerContext saved per request"));

        // NOW ASSERT WE CAN GET IT
        Map<String, String> endResult = triggerContextRepo.getNotificationTriggerContext(notificationTriggers.get(0).getId());

        assertTrue(endResult.containsKey("key1"));
        assertEquals("value1", endResult.get("key1"));
        assertTrue(endResult.containsKey("key2"));
        assertEquals("4455", endResult.get("key2"));
        assertTrue(endResult.containsKey("key3"));
        assertEquals("true", endResult.get("key3"));
    }

}