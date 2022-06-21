package com.solmod.notification.admin.data;

import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.MessageTemplateStatus;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class NotificationAdminRepositoryTest {

    @Spy
    @InjectMocks
    NotificationAdminRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we can create a MessageTemplate if it doesn't already exist per UniqueMessageTemplateId")
    void create_allClear() throws MessageTemplateAlreadyExistsException {
        MessageTemplate request = buildFullyPopulatedMessageTemplate();

        doReturn(null, new MessageTemplate()).when(repo).getMessageTemplate(any(UniqueMessageTemplateId.class));
        when(template.update(anyString(), anyMap())).thenReturn(1);

        // Call
        MessageTemplate created = repo.create(request);

        // Assert
        assertNotNull(created);
        verify(repo, times(1)).create(any(MessageTemplate.class));
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(2)).getMessageTemplate(any(UniqueMessageTemplateId.class));
    }

    @Test
    @DisplayName("Assert that we cannot create a MessageTemplate if fields are missing")
    void create_MissingFields(CapturedOutput captured) throws MessageTemplateAlreadyExistsException {
        MessageTemplate request = new MessageTemplate();
        request.setTenantId(345L);
        request.setEventSubject("Test");
        request.setEventVerb("Happening");
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        doReturn(emptyList()).when(repo).getMessageTemplates(any(MessageTemplate.class));

        // Call
        MessageTemplate created = repo.create(request);

        // Assert
        assertTrue(captured.getOut().contains("Failed attempt to save message template"));
        assertNull(created);
        // Find first to ensure it doesn't exist, use same find to load after save
        verify(repo, times(1)).getMessageTemplate(any(UniqueMessageTemplateId.class));
    }

    @Test
    @DisplayName("Assert we get an Exception if the MessageTemplate already exists per UniqueMessageTemplateId")
    void create_alreadyExists() throws MessageTemplateAlreadyExistsException {
        doReturn(new MessageTemplate()).when(repo).getMessageTemplate(any(UniqueMessageTemplateId.class));
        assertThrows(MessageTemplateAlreadyExistsException.class, () -> repo.create(new MessageTemplate()));
        verify(repo, times(1)).create(any(MessageTemplate.class));
    }

    @Test
    @DisplayName("Assert we can update a MessageTemplate whem there are no rules broken")
    void update_allClear() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate origFormOfRequest = new MessageTemplate();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setMessageTemplateStatus(MessageTemplateStatus.INACTIVE);
        origFormOfRequest.setEventSubject("OG_subject");
        origFormOfRequest.setEventVerb("OG_verb");
        origFormOfRequest.setContentLookupType(ContentLookupType.URL);
        origFormOfRequest.setContentKey("OG-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageTemplate request = new MessageTemplate();
        request.setId(155L);
        request.setMessageTemplateStatus(MessageTemplateStatus.INACTIVE);
        request.setEventSubject("new_subject");
        request.setEventVerb("new_verb");
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setContentKey("new-content-key");
        request.setRecipientContextKey("a-recipient-context-key");

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());
        doReturn(null).when(repo).getMessageTemplate(eq(UniqueMessageTemplateId.from(request)));

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Assert we can update a MessageTemplate whem there are no rules broken")
    void update_noChange() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate origFormOfRequest = new MessageTemplate();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setMessageTemplateStatus(MessageTemplateStatus.INACTIVE);
        origFormOfRequest.setEventSubject("OG_subject");
        origFormOfRequest.setEventVerb("OG_verb");
        origFormOfRequest.setContentLookupType(ContentLookupType.URL);
        origFormOfRequest.setContentKey("OG-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageTemplate request = new MessageTemplate();
        request.setId(155L);
        request.setMessageTemplateStatus(origFormOfRequest.getStatus());
        request.setEventSubject(origFormOfRequest.getEventSubject());
        request.setEventVerb(origFormOfRequest.getEventVerb());
        request.setContentLookupType(origFormOfRequest.getContentLookupType());
        request.setContentKey(origFormOfRequest.getContentKey());
        request.setRecipientContextKey(origFormOfRequest.getRecipientContextKey());

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());
        doReturn(null).when(repo).getMessageTemplate(eq(UniqueMessageTemplateId.from(request)));

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
    }

    @Test
    @DisplayName("Assert we get an exception and log an error if we attempt to update a MessageTemplate which doesn't exist per ID")
    @ExtendWith(OutputCaptureExtension.class)
    void update_templateNotFound(CapturedOutput out) {
        MessageTemplate request = new MessageTemplate();
        request.setId(15L);
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        doReturn(null).when(repo).getMessageTemplate(request.getId());

        assertThrows(MessageTemplateNonexistentException.class, () -> repo.update(request));
        assertTrue(out.getOut().contains("WARN"));
        assertTrue(out.getOut().contains("Attempt to update a MessageTemplate which does not exist"));
    }

    @Test
    @DisplayName("Assert we can't save a MessageTemplate and an informative message is logged and thrown when doing so " +
            "would collide with another per UniqueMessageTemplateId")
    void update_resultInConflict() {

        MessageTemplate request = new MessageTemplate();
        request.setId(155L);
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        request.setEventSubject("rit_test_subject");
        request.setEventVerb("rit_test_verb");
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setContentKey("a-content-key");
        request.setRecipientContextKey("a-recipient-context-key");

        MessageTemplate existing = new MessageTemplate();
        existing.setId(156L);
        existing.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        existing.setEventSubject(request.getEventSubject());
        existing.setEventVerb(request.getEventVerb());
        existing.setContentLookupType(ContentLookupType.STATIC);
        existing.setContentKey(request.getContentKey());
        existing.setRecipientContextKey(request.getRecipientContextKey());

        doReturn(request).when(repo).getMessageTemplate(request.getId());
        doReturn(existing).when(repo).getMessageTemplate(eq(UniqueMessageTemplateId.from(request)));

        // Call
        assertThrows(MessageTemplateAlreadyExistsException.class, () -> repo.update(request));
    }

    @Test
    @DisplayName("Assert that we can make 'colliding' changes to  MessageTemplate so long as there is only one Active across all duplicates")
    void update_noConflictDueToStatus() throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        MessageTemplate origFormOfRequest = new MessageTemplate();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setMessageTemplateStatus(MessageTemplateStatus.INACTIVE);
        origFormOfRequest.setEventSubject("not_colliding_subject");
        origFormOfRequest.setEventVerb("not_colliding_verb");
        origFormOfRequest.setContentLookupType(ContentLookupType.URL);
        origFormOfRequest.setContentKey("a-content-key");
        origFormOfRequest.setRecipientContextKey("a-recipient-context-key");

        MessageTemplate request = new MessageTemplate();
        request.setId(155L);
        request.setMessageTemplateStatus(MessageTemplateStatus.INACTIVE);
        request.setEventSubject("colliding_subject");
        request.setEventVerb("colliding_verb");
        request.setContentLookupType(ContentLookupType.CONTENT_KEY);
        request.setContentKey("a-content-key");
        request.setRecipientContextKey("a-recipient-context-key");

        MessageTemplate colliding = new MessageTemplate();
        colliding.setId(66532L);
        colliding.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        colliding.setEventSubject(request.getEventSubject());
        colliding.setEventVerb(request.getEventVerb());
        colliding.setContentLookupType(ContentLookupType.STATIC); // lookup type doesn't matter here
        colliding.setContentKey(request.getContentKey());
        colliding.setRecipientContextKey(request.getRecipientContextKey());

        doReturn(origFormOfRequest).when(repo).getMessageTemplate(request.getId());
        doReturn(colliding).when(repo).getMessageTemplate(eq(UniqueMessageTemplateId.from(request)));

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertEquals(3, result.size());
    }

    @Test
    void getMessageTemplate_byId_exists() {
    }

    @Test
    void getMessageTemplate_byId_notExists() {
    }

    @Test
    void getMessageTemplate_byId_nullId() {
    }

    @Test
    void getMessageTemplate_byUniqueId_exists() {
    }

    @Test
    void getMessageTemplate_byUniqueId_notExists() {
    }

    @Test
    void getMessageTemplate_byUniqueId_nullId() {
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria")
    void getMessageTemplates_byCriteriaInclId_exists() {
        /*        if (crit.getId() != null) {
            MessageTemplate messageTemplate = getMessageTemplate(crit.getId());
            if (messageTemplate != null)
                return List.of(messageTemplate);
            return Collections.emptyList();
        }

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "content_lookup_type, content_key, created_date, modified_date \n" +
                "FROM message_templates \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new MessageTemplateRowMapper()));
*/
        repo.getMessageTemplates()
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria")
    void getMessageTemplates_byCriteriaInclId_notExists() {
        
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria")
    void getMessageTemplates_byCriteriaExclId() {
        
    }

    private MessageTemplate buildFullyPopulatedMessageTemplate() {
        MessageTemplate request = new MessageTemplate();
        request.setTenantId(1L);
        request.setEventSubject("All");
        request.setEventVerb("Cleared");
        request.setRecipientContextKey("find.recipient.address.here");
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        request.setContentLookupType(ContentLookupType.STATIC);
        request.setContentKey("TEST_CONTENT_KEY");

        return request;
    }
}
