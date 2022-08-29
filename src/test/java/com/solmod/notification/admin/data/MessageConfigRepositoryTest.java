package com.solmod.notification.admin.data;

import com.solmod.notification.domain.MessageConfig;
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
public class MessageConfigRepositoryTest {

    @Spy
    @InjectMocks
    MessageConfigsRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Captor
    ArgumentCaptor<String> stringArgCaptor;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we can create a MessageConfig if it doesn't already exist per uniqueness rules")
    void create_allClear() throws DataCollisionException, DBRequestFailureException {
        MessageConfig request = buildFullyPopulatedMessageConfig();

        doReturn(null, new MessageConfig()).when(repo).getMessageConfig(any(MessageConfig.class));
        when(template.update(anyString(), anyMap())).thenReturn(1);

        // Call
        repo.create(request);

        verify(repo, times(1)).create(any(MessageConfig.class));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getMessageConfig(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert that we cannot create a MessageConfig and an error is logged if fields are missing")
    void create_MissingFields(CapturedOutput captured) {
        MessageConfig request = new MessageConfig();
        doReturn(emptyList()).when(repo).getMessageConfigs(any(MessageConfig.class));

        // Call
        assertThrows(DBRequestFailureException.class, () -> repo.create(request));

        // Assert
        assertTrue(captured.getOut().contains("Failed attempt to save component"));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getMessageConfig(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert we get an Exception if the MessageConfig already exists per uniqueness rules ")
    void create_alreadyExists() throws DataCollisionException, DBRequestFailureException {
        doReturn(new MessageConfig()).when(repo).getMessageConfig(any(MessageConfig.class));
        assertThrows(DataCollisionException.class, () -> repo.create(new MessageConfig()));
        verify(repo, times(1)).create(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert an inactive can be saved regardless any collisions")
    void update_allClear() throws ExpectedNotFoundException, DataCollisionException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setName("some-name-to-not-change");
        origFormOfRequest.setStatus(Status.ACTIVE);
        origFormOfRequest.setNotificationEventId(155L);

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setStatus(Status.INACTIVE);

        doReturn(origFormOfRequest).when(repo).getMessageConfig(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(1, result.size());
        verify(repo, never()).getMessageConfig(any(MessageConfig.class)); // No need to check dupes on inactive
    }

    @Test
    @DisplayName("Assert we can update a MessageConfig when there are no rules broken")
    void update_allClear_ignoreMissingFields() throws ExpectedNotFoundException, DataCollisionException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setName("this-will-stay");
        origFormOfRequest.setNotificationEventId(155L);
        origFormOfRequest.setStatus(Status.ACTIVE);

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setNotificationEventId(155L);
        request.setStatus(Status.INACTIVE);

        doReturn(origFormOfRequest).when(repo).getMessageConfig(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Assert no update is performed if there are no changes")
    void update_noChange() throws ExpectedNotFoundException, DataCollisionException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setName("no-change-name");
        origFormOfRequest.setStatus(Status.INACTIVE);

        MessageConfig request = new MessageConfig();
        request.setId(origFormOfRequest.getId());
        request.setName(origFormOfRequest.getName());
        request.setStatus(origFormOfRequest.getStatus());

        doReturn(origFormOfRequest).when(repo).getMessageConfig(request.getId());

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
        verify(repo, never()).getMessageConfig(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert we get an exception and log an error if we attempt to update a MessageConfig which doesn't exist per ID")
    @ExtendWith(OutputCaptureExtension.class)
    void update_templateNotFound(CapturedOutput out) {
        MessageConfig request = new MessageConfig();
        request.setId(15L);
        request.setStatus(Status.ACTIVE);
        doReturn(null).when(repo).getMessageConfig(request.getId());

        assertThrows(ExpectedNotFoundException.class, () -> repo.update(request));
        assertTrue(out.getOut().contains("WARN"));
        assertTrue(out.getOut().contains("Attempt to update a MessageConfig which does not exist"));
    }

    @Test
    @DisplayName("Assert we can't save a MessageConfig and an informative message is logged and thrown when doing so " +
            "would collide with another per uniqueness rules")
    void update_resultInConflict() {

        MessageConfig original = new MessageConfig();
        original.setId(155L);
        original.setNotificationEventId(25L);
        original.setName("unimportant_name");
        original.setStatus(Status.ACTIVE);

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setNotificationEventId(20L);
        request.setName("still-matterless");
        request.setStatus(Status.ACTIVE);

        MessageConfig conflicting = new MessageConfig();
        conflicting.setId(156L);
        conflicting.setNotificationEventId(20L);
        conflicting.setStatus(Status.ACTIVE);

        doReturn(original).when(repo).getMessageConfig(request.getId());
        doReturn(conflicting).when(repo).getMessageConfig(eq(request));

        // Call
        assertThrows(DataCollisionException.class, () -> repo.update(request));
    }

    @ParameterizedTest
    @CsvSource({"A,A,I", "I,I,A", "A,I,I"})
    @DisplayName("Assert that we can make 'colliding' changes to  MessageConfig so long as there is only one Active across all duplicates")
    void update_noConflictDueToStatus(String existingStatus, String requestStatus, String conflictingStatus)
            throws ExpectedNotFoundException, DataCollisionException {

        MessageConfig original = new MessageConfig();
        original.setId(155L);
        original.setName("some_og_test");
        original.setStatus(Status.fromCode(existingStatus));

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setName("I'dRatherThis");
        request.setStatus(Status.fromCode(requestStatus));

        MessageConfig conflicting = new MessageConfig();
        conflicting.setId(156L);
        conflicting.setName("ButThenThisOneWouldMakeDupe");
        conflicting.setStatus(Status.fromCode(conflictingStatus));

        doReturn(original).when(repo).getMessageConfig(request.getId());
        if (request.getStatus().equals(Status.ACTIVE)) {
            doReturn(conflicting).when(repo).getMessageConfig(eq(request));
        }

        // Call test
        Set<DataUtils.FieldUpdate> updates = repo.update(request);
        assertTrue(updates.size() >= 2);
        if (!request.getStatus().equals(Status.ACTIVE)) {
            verify(repo, never()).getMessageConfig(any(MessageConfig.class));
        } else {
            verify(repo, times(1)).getMessageConfig(any(MessageConfig.class));
        }
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getMessageConfig_byId_exists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(MessageConfigsRepository.MessageConfigResultSetExtractor.class)))
                .thenReturn(List.of(new MessageConfig()));

        // Test call
        MessageConfig result = repo.getMessageConfig(testId);

        assertNotNull(result);
        assertFalse(output.getOut().contains("WARN"));
        verify(template, times(1)).query(anyString(), anyMap(), any(MessageConfigsRepository.MessageConfigResultSetExtractor.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a warning log when we get a req by ID which doesn't exist")
    void getMessageConfig_byId_notExists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(MessageConfigsRepository.MessageConfigResultSetExtractor.class)))
                .thenReturn(emptyList());

        // Test call
        MessageConfig result = repo.getMessageConfig(15L);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("no results"));
        verify(template, times(1)).query(anyString(), eq(Map.of("id", testId)), any(MessageConfigsRepository.MessageConfigResultSetExtractor.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage(id) is supplied a null id")
    void getMessageConfig_byId_nullId(CapturedOutput output) {
        // Test call
        MessageConfig result = repo.getMessageConfig((Long) null);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("null id"));
        verify(template, never()).query(anyString(), anyMap(), any(MessageConfigsRepository.MessageConfigResultSetExtractor.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage by unique ID results in multiple")
    void getMessageConfig_byUniqueId_multipleExist(CapturedOutput output) {
        doReturn(List.of(new MessageConfig(), new MessageConfig())).when(repo).getMessageConfigs(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageConfig(new MessageConfig());

        assertNull(result);
        assertTrue(output.getOut().contains("ERROR"));
        assertTrue(output.getOut().contains("DATA INTEGRITY ERROR"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert there are no issues when a single MessageConfig is returned on getByUniqueId")
    void getMessageConfig_byUniqueId_exists(CapturedOutput output) {
        doReturn(List.of(new MessageConfig())).when(repo).getMessageConfigs(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageConfig(new MessageConfig());

        assertNotNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getMessageConfig_byUniqueId_notExists(CapturedOutput output) {
        doReturn(emptyList()).when(repo).getMessageConfigs(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageConfig(new MessageConfig());

        assertNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @DisplayName("Assert for getMessageConfig by null ID test not needed; @NonNull")
    void getMessageConfig_byUniqueId_nullId() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Assert all fields are considered in SQL statement when provided")
    void getMessageConfigs_byAllCriteria() {
        MessageConfig crit = new MessageConfig();
        crit.setName("all_crit_test");
        crit.setNotificationEventId(55L);
        crit.setStatus(Status.ACTIVE);

        // Test call
        repo.getMessageConfigs(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<MessageConfigsRepository.MessageConfigResultSetExtractor>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":notification_event_id"));
        assertTrue(stringArgCaptor.getValue().contains(":name"));
        assertTrue(stringArgCaptor.getValue().contains(":status"));
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria. By criteria ignores ID")
    void getMessageConfigs_byCrit() {
        MessageConfig crit = new MessageConfig();
        crit.setNotificationEventId(55L);
        crit.setStatus(null);

        // Test call
        repo.getMessageConfigs(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<MessageConfigsRepository.MessageConfigResultSetExtractor>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":notification_event_id"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
        assertFalse(stringArgCaptor.getValue().contains(":name"));
    }

    private MessageConfig buildFullyPopulatedMessageConfig() {
        MessageConfig request = new MessageConfig();
        request.setNotificationEventId(1L);
        request.setName("fully-pop-with-name");
        request.setStatus(Status.ACTIVE);

        return request;
    }
}
