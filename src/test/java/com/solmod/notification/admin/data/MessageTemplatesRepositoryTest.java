package com.solmod.notification.admin.data;

import com.solmod.notification.engine.domain.MessageContentPurpose;
import com.solmod.notification.engine.domain.MessageConfig;
import com.solmod.notification.engine.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
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
public class MessageTemplatesRepositoryTest {

    @Spy
    @InjectMocks
    MessageTemplatesRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Captor
    ArgumentCaptor<String> stringArgCaptor;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we can create a MessageTemplate if it doesn't already exist per UniqueMessageTemplateId")
    void create_allClear() throws MessageTemplateAlreadyExistsException, DBRequestFailureException {
        MessageConfig request = buildFullyPopulatedMessageTemplate();

        doReturn(null, new MessageConfig()).when(repo).getMessageTemplate(any(MessageConfig.class));
        when(template.update(anyString(), anyMap())).thenReturn(1);

        // Call
        repo.create(request);

        verify(repo, times(1)).create(any(MessageConfig.class));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getMessageTemplate(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert that we cannot create a MessageTemplate and an error is logged if fields are missing")
    void create_MissingFields(CapturedOutput captured) {
        MessageConfig request = new MessageConfig();
        request.setNotificationEventId(345L);
        request.setMessageContentPurpose(MessageContentPurpose.SMS);
        request.setStatus(Status.ACTIVE);
        doReturn(emptyList()).when(repo).getMessageTemplates(any(MessageConfig.class));

        // Call
        assertThrows(DBRequestFailureException.class, () -> repo.create(request));

        // Assert
        assertTrue(captured.getOut().contains("Failed attempt to save component"));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getMessageTemplate(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert we get an Exception if the MessageTemplate already exists per UniqueMessageTemplateId")
    void create_alreadyExists() throws MessageTemplateAlreadyExistsException, DBRequestFailureException {
        doReturn(new MessageConfig()).when(repo).getMessageTemplate(any(MessageConfig.class));
        assertThrows(MessageTemplateAlreadyExistsException.class, () -> repo.create(new MessageConfig()));
        verify(repo, times(1)).create(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert only supplied values are registered as change request")
    void update_allClear() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setStatus(Status.ACTIVE);
        origFormOfRequest.setNotificationEventId(155L);
        origFormOfRequest.setMessageContentPurpose(MessageContentPurpose.EMAIL);
        origFormOfRequest.setContentKey("OG-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setStatus(Status.INACTIVE);
        request.setRecipientContextKey(" ");

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(1, result.size());
        verify(repo, never()).getMessageTemplate(any(MessageConfig.class)); // No need to check dupes on inactive
    }

    @Test
    @DisplayName("Assert we can update a MessageTemplate when there are no rules broken")
    void update_allClear_ignoreMissingFields() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setNotificationEventId(155L);
        origFormOfRequest.setStatus(Status.ACTIVE);
        origFormOfRequest.setMessageContentPurpose(MessageContentPurpose.EMAIL);
        origFormOfRequest.setContentKey("OG-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setNotificationEventId(155L);
        request.setStatus(Status.INACTIVE);
        request.setMessageContentPurpose(MessageContentPurpose.SMS);
        request.setContentKey("new-content-key");
        request.setRecipientContextKey(origFormOfRequest.getRecipientContextKey());

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());

        // Test call
        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Assert no update is performed if there are no changes")
    void update_noChange() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageConfig origFormOfRequest = new MessageConfig();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setStatus(Status.INACTIVE);
        origFormOfRequest.setMessageContentPurpose(MessageContentPurpose.EMAIL);
        origFormOfRequest.setContentKey("OG-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageConfig request = new MessageConfig();
        request.setId(origFormOfRequest.getId());
        request.setStatus(origFormOfRequest.getStatus());
        request.setMessageContentPurpose(origFormOfRequest.getMessageContentPurpose());
        request.setContentKey(origFormOfRequest.getContentKey());
        request.setRecipientContextKey(origFormOfRequest.getRecipientContextKey());

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
        verify(repo, never()).getMessageTemplate(any(MessageConfig.class));
    }

    @Test
    @DisplayName("Assert we get an exception and log an error if we attempt to update a MessageTemplate which doesn't exist per ID")
    @ExtendWith(OutputCaptureExtension.class)
    void update_templateNotFound(CapturedOutput out) {
        MessageConfig request = new MessageConfig();
        request.setId(15L);
        request.setStatus(Status.ACTIVE);
        doReturn(null).when(repo).getMessageTemplate(request.getId());

        assertThrows(MessageTemplateNonexistentException.class, () -> repo.update(request));
        assertTrue(out.getOut().contains("WARN"));
        assertTrue(out.getOut().contains("Attempt to update a MessageTemplate which does not exist"));
    }

    @Test
    @DisplayName("Assert we can't save a MessageTemplate and an informative message is logged and thrown when doing so " +
            "would collide with another per UniqueMessageTemplateId")
    void update_resultInConflict() {

        MessageConfig original = new MessageConfig();
        original.setId(155L);
        original.setStatus(Status.ACTIVE);
        original.setMessageContentPurpose(MessageContentPurpose.SMS);
        original.setContentKey("a-content-key");
        original.setRecipientContextKey("a-recipient-context-key");

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setStatus(Status.ACTIVE);
        request.setMessageContentPurpose(MessageContentPurpose.SMS);
        request.setContentKey("b-content-key");
        request.setRecipientContextKey("b-recipient-context-key");

        MessageConfig conflicting = new MessageConfig();
        conflicting.setId(156L);
        conflicting.setStatus(Status.ACTIVE);
        conflicting.setMessageContentPurpose(MessageContentPurpose.SMS);
        conflicting.setContentKey(request.getContentKey());
        conflicting.setRecipientContextKey(request.getRecipientContextKey());

        doReturn(original).when(repo).getMessageTemplate(request.getId());
        doReturn(conflicting).when(repo).getMessageTemplate(eq(request));

        // Call
        assertThrows(MessageTemplateAlreadyExistsException.class, () -> repo.update(request));
    }

    @ParameterizedTest
    @CsvSource({"A,A,I", "I,I,A", "A,I,I"})
    @DisplayName("Assert that we can make 'colliding' changes to  MessageTemplate so long as there is only one Active across all duplicates")
    void update_noConflictDueToStatus(String existingStatus, String requestStatus, String conflictingStatus) throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageConfig original = new MessageConfig();
        original.setId(155L);
        original.setStatus(Status.fromCode(existingStatus));
        original.setMessageContentPurpose(MessageContentPurpose.SMS);
        original.setContentKey("a-content-key");
        original.setRecipientContextKey("a-recipient-context-key");

        MessageConfig request = new MessageConfig();
        request.setId(155L);
        request.setStatus(Status.fromCode(requestStatus));
        request.setMessageContentPurpose(MessageContentPurpose.SMS);
        request.setContentKey("b-content-key");
        request.setRecipientContextKey("b-recipient-context-key");

        MessageConfig conflicting = new MessageConfig();
        conflicting.setId(156L);
        conflicting.setStatus(Status.fromCode(conflictingStatus));
        conflicting.setMessageContentPurpose(MessageContentPurpose.SMS);
        conflicting.setContentKey(request.getContentKey());
        conflicting.setRecipientContextKey(request.getRecipientContextKey());

        doReturn(original).when(repo).getMessageTemplate(request.getId());
        if (request.getStatus().equals(Status.ACTIVE)) {
            doReturn(conflicting).when(repo).getMessageTemplate(eq(request));
        }

        // Call test
        Set<DataUtils.FieldUpdate> updates = repo.update(request);
        assertTrue(updates.size() >= 2);
        if (!request.getStatus().equals(Status.ACTIVE)) {
            verify(repo, never()).getMessageTemplate(any(MessageConfig.class));
        } else {
            verify(repo, times(1)).getMessageTemplate(any(MessageConfig.class));
        }
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getMessageTemplate_byId_exists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(MessageTemplatesRepository.MessageTemplateRowMapper.class)))
                .thenReturn(List.of(new MessageConfig()));

        // Test call
        MessageConfig result = repo.getMessageTemplate(testId);

        assertNotNull(result);
        assertFalse(output.getOut().contains("WARN"));
        verify(template, times(1)).query(anyString(), anyMap(), any(MessageTemplatesRepository.MessageTemplateRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a warning log when we get a req by ID which doesn't exist")
    void getMessageTemplate_byId_notExists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(MessageTemplatesRepository.MessageTemplateRowMapper.class)))
                .thenReturn(emptyList());

        // Test call
        MessageConfig result = repo.getMessageTemplate(15L);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("no results"));
        verify(template, times(1)).query(anyString(), eq(Map.of("id", testId)), any(MessageTemplatesRepository.MessageTemplateRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage(id) is supplied a null id")
    void getMessageTemplate_byId_nullId(CapturedOutput output) {
        // Test call
        MessageConfig result = repo.getMessageTemplate((Long) null);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("null id"));
        verify(template, never()).query(anyString(), anyMap(), any(MessageTemplatesRepository.MessageTemplateRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage by unique ID results in multiple")
    void getMessageTemplate_byUniqueId_multipleExist(CapturedOutput output) {
        doReturn(List.of(new MessageConfig(), new MessageConfig())).when(repo).getMessageTemplates(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageTemplate(new MessageConfig());

        assertNull(result);
        assertTrue(output.getOut().contains("ERROR"));
        assertTrue(output.getOut().contains("DATA INTEGRITY ERROR"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert there are no issues when a single MessageTemplate is returned on getByUniqueId")
    void getMessageTemplate_byUniqueId_exists(CapturedOutput output) {
        doReturn(List.of(new MessageConfig())).when(repo).getMessageTemplates(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageTemplate(new MessageConfig());

        assertNotNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void getMessageTemplate_byUniqueId_notExists(CapturedOutput output) {
        doReturn(emptyList()).when(repo).getMessageTemplates(any(MessageConfig.class));

        // test call
        MessageConfig result = repo.getMessageTemplate(new MessageConfig());

        assertNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
    }

    @Test
    @DisplayName("Assert for getMessageTemplate by null ID test not needed; @NonNull")
    void getMessageTemplate_byUniqueId_nullId() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Assert all fields are considered in SQL statement when provided")
    void getMessageTemplates_byAllCriteria() {
        MessageConfig crit = new MessageConfig();
        crit.setNotificationEventId(55L);
        crit.setRecipientContextKey("recipient_context_key");
        crit.setContentKey("content_key");
        crit.setMessageContentPurpose(MessageContentPurpose.SMS);
        crit.setStatus(Status.ACTIVE);

        // Test call
        repo.getMessageTemplates(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<MessageTemplatesRepository.MessageTemplateRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":notification_event_id"));
        assertTrue(stringArgCaptor.getValue().contains(":recipient_context_key"));
        assertTrue(stringArgCaptor.getValue().contains(":content_key"));
        assertTrue(stringArgCaptor.getValue().contains(":message_content_purpose"));
        assertTrue(stringArgCaptor.getValue().contains(":status"));
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria. By criteria ignores ID")
    void getMessageTemplates_byCrit() {
        MessageConfig crit = new MessageConfig();
        crit.setNotificationEventId(55L);
        crit.setRecipientContextKey("recipient_context_key");
        crit.setContentKey("content_key");
        crit.setMessageContentPurpose(MessageContentPurpose.SMS);
        crit.setStatus(null);

        // Test call
        repo.getMessageTemplates(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<MessageTemplatesRepository.MessageTemplateRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":notification_event_id"));
        assertTrue(stringArgCaptor.getValue().contains(":recipient_context_key"));
        assertTrue(stringArgCaptor.getValue().contains(":content_key"));
        assertTrue(stringArgCaptor.getValue().contains(":message_content_purpose"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
    }

    private MessageConfig buildFullyPopulatedMessageTemplate() {
        MessageConfig request = new MessageConfig();
        request.setNotificationEventId(1L);
        request.setRecipientContextKey("find.recipient.address.here");
        request.setStatus(Status.ACTIVE);
        request.setMessageContentPurpose(MessageContentPurpose.SMS);
        request.setContentKey("TEST_CONTENT_KEY");

        return request;
    }
}
