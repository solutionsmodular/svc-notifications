package com.solmod.notification.admin.data;

import com.solmod.notification.domain.NotificationEvent;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.DataCollisionException;
import com.solmod.notification.exception.ExpectedNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
public class NotificationEventsRepositoryTest {

    @Spy
    @InjectMocks
    NotificationEventsRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Captor
    ArgumentCaptor<String> stringArgCaptor;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we can create a NotificationEvent if it doesn't already exist per UniqueNotificationEventId")
    void create_allClear() throws DataCollisionException, DBRequestFailureException {
        NotificationEvent request = buildFullyPopulatedNotificationEvent();

        doReturn(null, new NotificationEvent()).when(repo).getNotificationEvent(any(NotificationEvent.class));

        // Call
        repo.create(request);

        verify(repo, times(1)).create(any(NotificationEvent.class));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Assert that we cannot create a NotificationEvent and an error is logged if fields are missing")
    void create_MissingFields(CapturedOutput captured) {
        NotificationEvent request = new NotificationEvent();
        request.setTenantId(345L);
//        request.setEventSubject("Test"); Missing required field
        request.setEventVerb("Happening");
        request.setStatus(Status.ACTIVE);
        doReturn(emptyList()).when(repo).getNotificationEvents(any(NotificationEvent.class));

        // Call
        assertThrows(DBRequestFailureException.class, () -> repo.create(request));

        // Assert
        assertTrue(captured.getOut().contains("Failed attempt to save component"));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Assert we get an Exception if the NotificationEvent already exists per UniqueNotificationEventId")
    void create_alreadyExists() throws DataCollisionException, DBRequestFailureException {
        doReturn(new NotificationEvent()).when(repo).getNotificationEvent(any(NotificationEvent.class));
        assertThrows(DataCollisionException.class, () -> repo.create(new NotificationEvent()));
        verify(repo, times(1)).create(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Assert only supplied values are registered as change request")
    void update_allClear() throws ExpectedNotFoundException, DataCollisionException {
        NotificationEvent origFormOfRequest = new NotificationEvent();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setStatus(Status.INACTIVE);
        origFormOfRequest.setEventSubject("OG_subject");
        origFormOfRequest.setEventVerb("OG_verb");

        NotificationEvent request = new NotificationEvent();
        request.setId(155L);
        request.setStatus(Status.INACTIVE);
        request.setEventSubject("new_subject");
        request.setEventVerb("new_verb");

        doReturn(origFormOfRequest).when(repo).getNotificationEvent(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(2, result.size());
        verify(repo, never()).getNotificationEvent(any(NotificationEvent.class)); // No need to check dupes on inactive
    }

    @Test
    @DisplayName("Assert we can update a NotificationEvent when there are no rules broken")
    void update_allClear_ignoreMissingFields() throws ExpectedNotFoundException, DataCollisionException {
        NotificationEvent origFormOfRequest = new NotificationEvent();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setStatus(Status.INACTIVE);
        origFormOfRequest.setEventSubject("OG_subject");
        origFormOfRequest.setEventVerb("OG_verb");
        origFormOfRequest.setTenantId(18L);

        NotificationEvent request = new NotificationEvent();
        request.setId(155L);
        request.setStatus(Status.INACTIVE);
        request.setEventSubject("new_subject");
        request.setEventVerb("new_verb");
        request.setTenantId(888888L);

        doReturn(origFormOfRequest).when(repo).getNotificationEvent(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        verify(template, times(1)).update(stringArgCaptor.capture(), anyMap());
        assertEquals(2, result.size());
        assertTrue(stringArgCaptor.getValue().contains(":event_subject"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
        verify(repo, never()).getNotificationEvent(any(NotificationEvent.class)); // No need to check dupes on inactive
    }

    @Test
    @DisplayName("Assert no update is performed if there are no changes")
    void update_noChange() throws ExpectedNotFoundException, DataCollisionException {
        NotificationEvent origFormOfRequest = new NotificationEvent();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setStatus(Status.INACTIVE);
        origFormOfRequest.setEventSubject("OG_subject");
        origFormOfRequest.setEventVerb("OG_verb");

        NotificationEvent request = new NotificationEvent();
        request.setId(origFormOfRequest.getId());
        request.setStatus(origFormOfRequest.getStatus());
        request.setEventSubject(origFormOfRequest.getEventSubject());
        request.setEventVerb(origFormOfRequest.getEventVerb());

        doReturn(origFormOfRequest).when(repo).getNotificationEvent(request.getId());

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
        verify(repo, never()).getNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Assert we get an exception and log an error if we attempt to update a NotificationEvent which doesn't exist per ID")
    @ExtendWith(OutputCaptureExtension.class)
    void update_contextNotFound(CapturedOutput out) {
        NotificationEvent request = new NotificationEvent();
        request.setId(15L);
        request.setStatus(Status.ACTIVE);
        doReturn(null).when(repo).getNotificationEvent(request.getId());

        assertThrows(ExpectedNotFoundException.class, () -> repo.update(request));
        assertTrue(out.getOut().contains("WARN"));
        assertTrue(out.getOut().contains("Attempt to update a NotificationEvent which does not exist"));
    }

    @Test
    @DisplayName("Assert we can't save a NotificationEvent and an informative message is logged and thrown when doing so " +
            "would collide with another per unique requirements")
    void update_resultInConflict() {

        NotificationEvent existing = new NotificationEvent();
        existing.setId(155L);
        existing.setStatus(Status.ACTIVE);
        existing.setEventSubject("someOriginalSubject");
        existing.setEventVerb("someOriginalVerb");

        NotificationEvent request = new NotificationEvent();
        request.setId(155L);
        request.setStatus(Status.ACTIVE);
        request.setEventSubject("someChangedSubject");
        request.setEventVerb("someChangedVerb");

        NotificationEvent conflicting = new NotificationEvent();
        conflicting.setId(156L);
        conflicting.setStatus(Status.ACTIVE);
        conflicting.setEventSubject(request.getEventSubject());
        conflicting.setEventVerb(request.getEventVerb());

        doReturn(existing).when(repo).getNotificationEvent(request.getId());
        doReturn(conflicting).when(repo).getNotificationEvent(eq(request));

        // Call
        assertThrows(DataCollisionException.class, () -> repo.update(request));
    }

    @ParameterizedTest
    @CsvSource({"A,A,I", "I,I,A", "A,I,I"})
    @DisplayName("Assert that we can make 'colliding' changes to  NotificationEvent so long as there is only one Active across all duplicates")
    void update_noConflictDueToStatus(String existingStatus, String requestStatus, String conflictingStatus) throws ExpectedNotFoundException, DataCollisionException {
        NotificationEvent existing = new NotificationEvent();
        existing.setId(155L);
        existing.setStatus(Status.fromCode(existingStatus));
        existing.setEventSubject("someOriginalSubject");
        existing.setEventVerb("someOriginalVerb");

        NotificationEvent request = new NotificationEvent();
        request.setId(155L);
        request.setStatus(Status.fromCode(requestStatus));
        request.setEventSubject("someChangedSubject");
        request.setEventVerb("someChangedVerb");

        NotificationEvent wouldBeConflicting = new NotificationEvent();
        wouldBeConflicting.setId(156L);
        wouldBeConflicting.setStatus(Status.fromCode(conflictingStatus));
        wouldBeConflicting.setEventSubject(request.getEventSubject());
        wouldBeConflicting.setEventVerb(request.getEventVerb());

        doReturn(existing).when(repo).getNotificationEvent(request.getId());
        if (request.getStatus().equals(Status.ACTIVE)) {
            doReturn(wouldBeConflicting).when(repo).getNotificationEvent(eq(request));
        }

        // Call
        Set<DataUtils.FieldUpdate> update = repo.update(request);
        assertTrue(update.size() >= 2);
        if (!request.getStatus().equals(Status.ACTIVE)) {
            verify(repo, never()).getNotificationEvent(any(NotificationEvent.class));
        } else {
            verify(repo, times(1)).getNotificationEvent(any(NotificationEvent.class));
        }
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getNotificationEvent_byId_exists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationEventsRepository.NotificationEventRowMapper.class)))
                .thenReturn(List.of(new NotificationEvent()));

        // Test call
        NotificationEvent result = repo.getNotificationEvent(testId);

        assertNotNull(result);
        assertFalse(output.getOut().contains("WARN"));
        verify(template, times(1)).query(anyString(), anyMap(), any(NotificationEventsRepository.NotificationEventRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a warning log when we get a req by ID which doesn't exist")
    void getNotificationEvent_byId_notExists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationEventsRepository.NotificationEventRowMapper.class)))
                .thenReturn(emptyList());

        // Test call
        NotificationEvent result = repo.getNotificationEvent(15L);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("no results"));
        verify(template, times(1)).query(anyString(), eq(Map.of("id", testId)), any(NotificationEventsRepository.NotificationEventRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage(id) is supplied a null id")
    void getNotificationEvent_byId_nullId(CapturedOutput output) {
        // Test call
        NotificationEvent result = repo.getNotificationEvent((Long) null);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("null id"));
        verify(template, never()).query(anyString(), anyMap(), any(NotificationEventsRepository.NotificationEventRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage by unique ID results in multiple")
    void getNotificationEvent_byUniqueId_multipleExist(CapturedOutput output) {
        doReturn(List.of(new NotificationEvent(), new NotificationEvent())).when(repo).getNotificationEvents(any(NotificationEvent.class));

        // test call
        NotificationEvent result = repo.getNotificationEvent(new NotificationEvent());

        assertNull(result);
        assertTrue(output.getOut().contains("ERROR"));
        assertTrue(output.getOut().contains("DATA INTEGRITY ERROR"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert there are no issues when a single NotificationEvent is returned on getByUniqueId")
    void getNotificationEvent_byUniqueId_exists(CapturedOutput output) {
        doReturn(List.of(new NotificationEvent())).when(repo).getNotificationEvents(any(NotificationEvent.class));

        // test call
        NotificationEvent result = repo.getNotificationEvent(new NotificationEvent());

        assertNotNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getNotificationEvent_byUniqueId_notExists(CapturedOutput output) {
        doReturn(emptyList()).when(repo).getNotificationEvents(any(NotificationEvent.class));

        // test call
        NotificationEvent result = repo.getNotificationEvent(new NotificationEvent());

        assertNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @DisplayName("Assert for getNotificationEvent by null ID test not needed; @NonNull")
    void getNotificationEvent_byUniqueId_nullId() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Assert all params are considered in SQL statement when provided. By criteria ignores ID")
    void getNotificationEvents_byAllCrit() {
        NotificationEvent crit = new NotificationEvent();
        crit.setTenantId(55L);
        crit.setEventSubject("event_subject");
        crit.setEventVerb("event_verb");
        crit.setStatus(Status.ACTIVE);

        // Test call
        repo.getNotificationEvents(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<NotificationEventsRepository.NotificationEventRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":tenant_id"));
        assertTrue(stringArgCaptor.getValue().contains(":event_subject"));
        assertTrue(stringArgCaptor.getValue().contains(":event_verb"));
        assertTrue(stringArgCaptor.getValue().contains(":status"));
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria. By criteria ignores ID")
    void getNotificationEvents_byCrit() {
        NotificationEvent crit = new NotificationEvent();
        crit.setTenantId(55L);
        crit.setEventSubject("event_subject");
        crit.setEventVerb("event_verb");
        crit.setStatus(null);

        // Test call
        repo.getNotificationEvents(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<NotificationEventsRepository.NotificationEventRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":tenant_id"));
        assertTrue(stringArgCaptor.getValue().contains(":event_subject"));
        assertTrue(stringArgCaptor.getValue().contains(":event_verb"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
    }

    private NotificationEvent buildFullyPopulatedNotificationEvent() {
        NotificationEvent request = new NotificationEvent();
        request.setTenantId(1L);
        request.setEventSubject("All");
        request.setEventVerb("Cleared");
        request.setStatus(Status.ACTIVE);

        return request;
    }
}
