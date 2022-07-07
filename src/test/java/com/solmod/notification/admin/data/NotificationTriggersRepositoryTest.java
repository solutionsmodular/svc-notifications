package com.solmod.notification.admin.data;

import com.solmod.notification.domain.NotificationTrigger;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class NotificationTriggersRepositoryTest {

    @Spy
    @InjectMocks
    NotificationTriggersRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Captor
    ArgumentCaptor<String> stringArgCaptor;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we cannot create a NotificationTrigger and an error is logged if fields are missing")
    void create_MissingFields(CapturedOutput captured) {
        NotificationTrigger request = new NotificationTrigger();
        // request.setNotificationEventId(345L);
        request.setUid("xoxoxoxoxoxoxoxooxoxoxoxoxoxox");
        request.setStatus(Status.ACTIVE);

        // Call
        assertThrows(DBRequestFailureException.class, () -> repo.create(request));

        // Assert
        assertTrue(captured.getOut().contains("Failed attempt to save NotificationTrigger"));
    }

    @Test
    @DisplayName("Assert no update is performed if there are no changes")
    void update_noChange() throws NotificationTriggerNonexistentException {
        NotificationTrigger origFormOfRequest = new NotificationTrigger();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setNotificationEventId(255L);
        origFormOfRequest.setUid("some-uid");
        origFormOfRequest.setStatus(Status.INACTIVE);

        NotificationTrigger request = new NotificationTrigger();
        request.setId(origFormOfRequest.getId());
        request.setStatus(origFormOfRequest.getStatus());
        request.setUid(origFormOfRequest.getUid());
        request.setNotificationEventId(origFormOfRequest.getNotificationEventId());

        doReturn(origFormOfRequest).when(repo).getNotificationTrigger(request.getId());

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
    }

    @Test
    @DisplayName("Assert we get an exception and log an error if we attempt to update a NotificationTrigger which doesn't exist per ID")
    @ExtendWith(OutputCaptureExtension.class)
    void update_contextNotFound(CapturedOutput out) {
        NotificationTrigger request = new NotificationTrigger();
        request.setId(15L);
        request.setStatus(Status.ACTIVE);
        doReturn(null).when(repo).getNotificationTrigger(request.getId());

        assertThrows(NotificationTriggerNonexistentException.class, () -> repo.update(request));
        assertTrue(out.getOut().contains("WARN"));
        assertTrue(out.getOut().contains("Attempt to update a NotificationTrigger which does not exist"));
    }

    @Test
    void getNotificationTrigger_byId_exists() {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationTriggersRepository.NotificationTriggerRowMapper.class)))
                .thenReturn(List.of(new NotificationTrigger()));

        // Test call
        NotificationTrigger result = repo.getNotificationTrigger(testId);

        assertNotNull(result);
        verify(template, times(1)).query(anyString(), anyMap(), any(NotificationTriggersRepository.NotificationTriggerRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a warning log when we get a req by ID which doesn't exist")
    void getNotificationTrigger_byId_notExists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationTriggersRepository.NotificationTriggerRowMapper.class)))
                .thenReturn(emptyList());

        // Test call
        NotificationTrigger result = repo.getNotificationTrigger(15L);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("no results"));
        verify(template, times(1)).query(anyString(), eq(Map.of("id", testId)), any(NotificationTriggersRepository.NotificationTriggerRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage(id) is supplied a null id")
    void getNotificationTrigger_byId_nullId(CapturedOutput output) {
        // Test call
        NotificationTrigger result = repo.getNotificationTrigger(null);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("null id"));
        verify(template, never()).query(anyString(), anyMap(), any(NotificationTriggersRepository.NotificationTriggerRowMapper.class));
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria. By criteria ignores ID")
    void getNotificationTriggers_byCrit() {
        NotificationTrigger crit = new NotificationTrigger();
        // crit.setNotificationEventId(55L);
        crit.setUid("some-uid");
        crit.setStatus(null);

        // Test call
        repo.getNotificationTriggers(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<NotificationTriggersRepository.NotificationTriggerRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertFalse(stringArgCaptor.getValue().contains(":notification_event_id"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
    }
}
