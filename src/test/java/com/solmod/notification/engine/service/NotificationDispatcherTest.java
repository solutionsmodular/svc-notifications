package com.solmod.notification.engine.service;

import com.solmod.commons.StringifyException;
import com.solmod.notification.admin.data.*;
import com.solmod.notification.domain.*;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.solmod.commons.ObjectUtils.flatten;
import static com.solmod.notification.domain.Status.NO_OP;
import static com.solmod.notification.domain.Status.PENDING_DELIVERY;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class NotificationDispatcherTest {

    @InjectMocks
    @Spy
    NotificationDispatcher dispatcher;

    @Mock
    NotificationEventsRepository neRepo;
    @Mock
    MessageTemplatesRepository mtRepo;
    @Mock
    NotificationTriggersRepository ntRepo;
    @Mock
    NotificationTriggerContextRepository ntcRepo;
    @Mock
    NotificationDeliveriesRepository ndRepo;

    @Captor
    ArgumentCaptor<NotificationTrigger> triggerCaptor;
    @Captor
    ArgumentCaptor<NotificationDelivery> deliveryCaptor;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @Test
    @DisplayName("Assert we get null when there are no events registered for the message")
    void apply_noRegisteredEvent() throws StringifyException {
        NotificationEvent criteria = buildNotificationEvent();
        when(neRepo.getNotificationEvent(criteria)).thenReturn(null);

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("my-email@somewhere.com", "Jimbo"));
        SolMessage request = buildSolMessage(criteria, context);

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(criteria);
        assertNull(result);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we receive an empty array and are warned when there's an event that there are no triggers for")
    void apply_noRegisteredTemplates(CapturedOutput output) throws StringifyException {
        NotificationEvent event = buildNotificationEvent();

        MessageTemplate messagesCriteria = buildTemplateCriteria(event.getId());

        when(neRepo.getNotificationEvent(event)).thenReturn(event);
        when(mtRepo.getMessageTemplates(messagesCriteria)).thenReturn(emptyList());

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("my-email@somewhere.com", "Jimbo"));
        SolMessage request = buildSolMessage(event, context);

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(event);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("NotificationEvent with no MessageTemplates"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Verify that, when the context is missing context, we log and status PENDING_CONTEXT")
    void apply_contextMissingRecpientKey(CapturedOutput output) throws StringifyException, NotificationTriggerNonexistentException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageTemplate repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageTemplate template = buildTemplate();

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mtRepo.getMessageTemplates(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest(null, "Jimbo"));
        SolMessage request = buildSolMessage(eventCriteria, context);
        // Make so the context is missing criteria needed for template

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(eventCriteria);
        verify(ntRepo, times(1)).update(triggerCaptor.capture());
        assertNull(result);
        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("More context is needed"));
        assertEquals(Status.PENDING_CONTEXT, triggerCaptor.getValue().getStatus());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Verify that, when the context has empty recipient value, we log and status PENDING_CONTEXT")
    void apply_contextEmptyRecpientKey(CapturedOutput output) throws StringifyException, NotificationTriggerNonexistentException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageTemplate repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageTemplate template = buildTemplate();

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mtRepo.getMessageTemplates(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest(" ", "Jimbo"));
        SolMessage request = buildSolMessage(eventCriteria, context);
        // Make so the context is missing criteria needed for template

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(eventCriteria);
        verify(ntRepo, times(1)).update(triggerCaptor.capture());
        assertNull(result);
        assertTrue(output.getOut().contains("INFO"));
        assertTrue(output.getOut().contains("More context is needed"));
        assertTrue(output.getOut().contains("Not enough context to determine recipient addy"));
        assertEquals(Status.PENDING_CONTEXT, triggerCaptor.getValue().getStatus());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert when there are MessageTemplates and context contains required keys, unmatching values, nothing happens")
    void apply_completeContextNoMatchDeliveryCriteria(CapturedOutput output) throws StringifyException, DBRequestFailureException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageTemplate repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageTemplate template = buildTemplate();
/*
        // For clarity; here are the values we need from the context
        template.setRecipientContextKey("message.data.person.email");
        template.setDeliveryCriteria(Map.of("message.data.person.name", "Jimbo"));
*/

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mtRepo.getMessageTemplates(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("cool@email.com", "DefinitelyNotJimbo"));
        SolMessage request = buildSolMessage(eventCriteria, context);
        // Make so the context is missing criteria needed for template

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(eventCriteria);
        verify(ntRepo, times(1)).create(triggerCaptor.capture());
        assertNull(result);
        assertFalse(output.getOut().contains("ERROR"));
        assertFalse(output.getOut().contains("WARN"));
        assertEquals(NO_OP, triggerCaptor.getValue().getStatus());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void apply_deliveriesPersisted(CapturedOutput output) throws DBRequestFailureException, StringifyException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageTemplate repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageTemplate template = buildTemplate();

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mtRepo.getMessageTemplates(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest qualifyingContext = new MessageContextTest(
                new PersonContextTest("cool@email.com", "Jimbo"));
        SolMessage request = buildSolMessage(eventCriteria, qualifyingContext);
        // Make so the context is missing criteria needed for template

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(eventCriteria);
        verify(ntRepo, times(1)).update(triggerCaptor.capture());
        verify(ndRepo, times(1)).create(deliveryCaptor.capture());
        assertNull(result);
        assertTrue(output.getOut().contains("INFO"));
        assertEquals(PENDING_DELIVERY, triggerCaptor.getValue().getStatus());
        assertEquals(Status.PENDING_PERMISSION, deliveryCaptor.getValue().getStatus());
    }

    @Test
    void persistRelevantContext() {
    }

    @Test
    void determineAndBuildDeliveries() {
    }

    @Test
    void getRelatedMessageTemplates() {
    }

    @Test
    void filterByContext() {
    }

    private MessageTemplate buildTemplateCriteria(Long eventId) {
        MessageTemplate messagesCriteria = new MessageTemplate();
        messagesCriteria.setStatus(Status.ACTIVE);
        messagesCriteria.setNotificationEventId(eventId);
        return messagesCriteria;
    }

    private NotificationEvent buildNotificationEvent() {
        NotificationEvent eventCriteria = new NotificationEvent();
        eventCriteria.setTenantId(15L);
        eventCriteria.setEventSubject("some-subject");
        eventCriteria.setEventVerb("some_verb");
        eventCriteria.setStatus(Status.ACTIVE);
        return eventCriteria;
    }

    private MessageTemplate buildTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setNotificationEventId(15L);
        template.setRecipientContextKey("message.data.person.email");
        template.setDeliveryCriteria(Map.of("message.data.person.firstName", "Jimbo"));

        return template;
    }

    private SolMessage buildSolMessage(NotificationEvent eventCriteria, MessageContextTest context) throws StringifyException {
        SolMessage request = new SolMessage();
        request.setTenantId(eventCriteria.getTenantId());
        request.setSubject(eventCriteria.getEventSubject());
        request.setVerb(eventCriteria.getEventVerb());

        request.setData(context);
        return request;
    }

    static class MessageContextTest{
        private PersonContextTest person;

        public MessageContextTest(PersonContextTest person) {
            this.person = person;
        }

        public PersonContextTest getPerson() {
            return person;
        }

        public void setPerson(PersonContextTest person) {
            this.person = person;
        }
    }

    static class PersonContextTest {
        private String email;
        private String firstName;

        public PersonContextTest(String email, String firstName) {
            this.email = email;
            this.firstName = firstName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }
}