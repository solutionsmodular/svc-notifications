package com.solmod.notification.admin.data;

import com.solmod.notification.domain.NotificationEvent;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.NotificationEventAlreadyExistsException;
import com.solmod.notification.exception.NotificationEventNonexistentException;
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
 * For all other tests, see {@link NotificationEventsRepositoryTest} which will assert business logic and such
 */
@Sql(scripts = {"classpath:/scripts/notification-admin-tests.sql"})
@SpringBootTest
@Transactional
class NotificationEventsRepositoryIntegrationTest {

    @Autowired
    NotificationEventsRepository contextRepository;

    @Test
    @DisplayName("Testing create. Happy day case in integration test, only")
    @ExtendWith(OutputCaptureExtension.class)
    void testCreate(CapturedOutput output) throws NotificationEventAlreadyExistsException {
        NotificationEvent request = new NotificationEvent();
        request.setTenantId(1L);
        request.setEventSubject("Something");
        request.setEventVerb("Occurred");
        request.setStatus(Status.ACTIVE);

        // Call
        contextRepository.create(request);

        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("created"));
    }

    @Test
    @DisplayName("Testing Get by Criteria AND update. Happy day case in integration test, only. This test uses data from notification-admin-tests.sql")
    void testGetByCriteriaAndUpdate() throws NotificationEventNonexistentException, NotificationEventAlreadyExistsException {
        NotificationEvent criteria = new NotificationEvent();
        criteria.setEventSubject("ORDER");
        criteria.setEventVerb("CREATED");
        NotificationEvent live = getLiveTestContext(criteria);

        NotificationEvent request = new NotificationEvent();
        request.setId(live.getId());
        request.setEventVerb("ANOTHER_VERB");
        request.setEventSubject("  "); // Empty field is not interpreted as a valid change and must be ignored

        Set<DataUtils.FieldUpdate> fieldsUpdated = contextRepository.update(request);
        assertEquals(1, fieldsUpdated.size());

        NotificationEvent updated = contextRepository.getNotificationEvent(request.getId());
        // Assert intended fields are updated
        assertEquals(request.getEventVerb(), updated.getEventVerb());
        // Assert other fields are changed
        assertEquals(live.getStatus(), updated.getStatus());
        assertEquals(live.getEventSubject(), updated.getEventSubject());
    }

    private NotificationEvent getLiveTestContext(NotificationEvent criteria) {
        NotificationEvent existing = contextRepository.getNotificationEvent(criteria);
        return existing;
    }
}