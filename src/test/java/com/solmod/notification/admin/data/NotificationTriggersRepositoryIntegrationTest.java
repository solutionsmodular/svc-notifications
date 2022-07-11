package com.solmod.notification.admin.data;

import com.solmod.notification.domain.NotificationEvent;
import com.solmod.notification.domain.NotificationTrigger;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test will perform tests against a DB to assert bare requirements for DB statements.
 * For all other tests, see {@link NotificationTriggersRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class NotificationTriggersRepositoryIntegrationTest {

    @Autowired
    NotificationTriggersRepository triggerRepo;

    @Autowired
    NotificationEventsRepository eventRepo;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws DBRequestFailureException {

        NotificationTrigger request = new NotificationTrigger();
        NotificationEvent event = getLiveNotificationEvent();
        request.setNotificationEventId(event.getId());
        request.setUid("1234Infinity");
        request.setContext(Map.of("key", "value"));
        request.setStatus(Status.ACTIVE);

        // Call
        triggerRepo.create(request);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws NotificationTriggerNonexistentException {
        NotificationTrigger criteria = new NotificationTrigger();
        criteria.setUid("existing-uid");

        List<NotificationTrigger> notificationTriggers = triggerRepo.getNotificationTriggers(criteria);

        assertEquals(1, notificationTriggers.size());

        NotificationTrigger request = new NotificationTrigger();
        request.setId(notificationTriggers.get(0).getId());
        request.setUid("different-uid");
        request.setStatus(Status.INACTIVE);

        Set<DataUtils.FieldUpdate> fieldsUpdated = triggerRepo.update(request);
        assertEquals(2, fieldsUpdated.size());

        NotificationTrigger updated = this.triggerRepo.getNotificationTrigger(request.getId());
        // Assert intended fields are updated
        assertEquals(request.getUid(), updated.getUid());
        assertEquals(request.getStatus(), updated.getStatus());
    }

    private NotificationEvent getLiveNotificationEvent() {
        NotificationEvent criteria = new NotificationEvent();
        criteria.setEventSubject("ORDER");
        criteria.setEventVerb("CREATED");
        return eventRepo.getNotificationEvents(criteria).get(0);
    }
}