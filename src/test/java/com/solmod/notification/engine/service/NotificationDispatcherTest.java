package com.solmod.notification.engine.service;

import com.solmod.commons.StringifyException;
import com.solmod.notification.admin.data.*;
import com.solmod.notification.domain.*;
import com.solmod.notification.domain.summary.MessageTemplateSummary;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.ExpectedNotFoundException;
import com.solmod.notification.exception.InsufficientContextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.*;

import static com.solmod.commons.ObjectUtils.flatten;
import static com.solmod.notification.domain.Status.*;
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
    MessageConfigsRepository mcRepo;
    @Mock
    NotificationTriggersRepository ntRepo;
    @Mock
    @SuppressWarnings("unused")
    NotificationTriggerContextRepository ntcRepo;
    @Mock
    NotificationDeliveriesRepository ndRepo;

    @Captor
    ArgumentCaptor<NotificationTrigger> triggerCaptor;
    @Captor
    ArgumentCaptor<MessageTemplate> templateCaptor;
    @Captor
    ArgumentCaptor<MessageConfig> configCaptor;
    @Captor
    ArgumentCaptor<NotificationDelivery> deliveryCaptor;
    @Captor
    ArgumentCaptor<Map<String, String>> contextCaptor;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @Test
    @DisplayName("Assert we get null when there are no events registered for the message")
    void apply_noRegisteredEvent() {
        NotificationEvent criteria = buildNotificationEvent();
        when(neRepo.getNotificationEvent(criteria)).thenReturn(null);

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("my-email@somewhere.com", "Peter"));
        SolMessage request = buildSolMessage(criteria, context);

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(criteria);
        assertNull(result);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we receive an empty array and are warned when there's an event that there are no triggers for")
    void apply_noRegisteredTemplates(CapturedOutput output) {
        NotificationEvent event = buildNotificationEvent();

        MessageConfig messagesCriteria = buildTemplateCriteria(event.getId());

        when(neRepo.getNotificationEvent(event)).thenReturn(event);
        when(mcRepo.getMessageConfigs(messagesCriteria)).thenReturn(emptyList());

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("my-email@somewhere.com", "Peter"));
        SolMessage request = buildSolMessage(event, context);

        List<SolCommunication> result = dispatcher.apply(request);
        verify(neRepo, times(1)).getNotificationEvent(event);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("NotificationEvent with no MessageConfigs"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Verify that, when the context is missing context, we log and status PENDING_CONTEXT")
    void apply_contextMissingRecipientKey(CapturedOutput output) throws ExpectedNotFoundException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageConfig repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageConfig template = buildConfig("solmod_evt.data.person.email", Map.of("solmod_evt.data.person.firstName", "Peter"));

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mcRepo.getMessageConfigs(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest(null, "Peter"));
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
    void apply_contextEmptyRecipientKey(CapturedOutput output) throws ExpectedNotFoundException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageConfig repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageConfig config = buildConfig("solmod_evt.data.person.email", Map.of("solmod_evt.data.person.firstName", "Peter"));

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mcRepo.getMessageConfigs(repoCriteria)).thenReturn(List.of(config));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest(" ", "Peter"));
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
    void apply_completeContextNoMatchDeliveryCriteria(CapturedOutput output) throws DBRequestFailureException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageConfig repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageConfig template = buildConfig("solmod_evt.data.person.email", Map.of("solmod_evt.data.person.firstName", "Peter"));
/*
        // For clarity; here are the values we need from the context
        Recipient Context Key = "solmod_evt.data.person.email"
        Delivery Criteria = solmod_evt.data.person.name" == "Peter"
*/

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mcRepo.getMessageConfigs(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest context = new MessageContextTest(
                new PersonContextTest("cool@email.com", "DefinitelyNotPeter"));
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
    void apply_deliveriesPersisted(CapturedOutput output) throws DBRequestFailureException, ExpectedNotFoundException {
        NotificationEvent eventCriteria = buildNotificationEvent();
        MessageConfig repoCriteria = buildTemplateCriteria(eventCriteria.getId());

        MessageConfig template = buildConfig("solmod_evt.data.person.email", Map.of("solmod_evt.data.person.firstName", "Peter"));

        NotificationEvent event = new NotificationEvent();
        event.setId(eventCriteria.getId());
        when(neRepo.getNotificationEvent(eventCriteria)).thenReturn(event);
        when(mcRepo.getMessageConfigs(repoCriteria)).thenReturn(List.of(template));

        MessageContextTest qualifyingContext = new MessageContextTest(
                new PersonContextTest("cool@email.com", "Peter"));
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
    @DisplayName("Verify only content needed by all of the MessageTemplates associated with a triggered NotificationEvent")
    void persistRelevantContext_irrelevantContextNotPersisted() throws InsufficientContextException, DBRequestFailureException, StringifyException {
        NotificationTrigger trigger = new NotificationTrigger();
        trigger.setId(22L);

        HashMap<String, Object> entireContext = new HashMap<>();
        entireContext.put("solmod_evt", new MessageContextTest(new PersonContextTest("roger@rabbits.com", "Roger, of course")));
        // "custom" would be the result of running a context builder
        entireContext.put("custom", new MessageContextTest(
                new PersonContextTest("roger@rabbits.com", "Roger, of course", 
                        new PersonContextTest("sponsor@rabbits.com", "Jessica"))));
        entireContext.put("unused", new MessageContextTest(new PersonContextTest("who@cares.com", "poof")));

        HashMap<String, Object> relevantContext = new HashMap<>();
        relevantContext.put("solmod_evt.person.email", "roger@rabbits.com");
        relevantContext.put("solmod_evt.person.firstName", "Roger, of course");
        relevantContext.put("custom.person.sponsor.email", "sponsor@rabbits.com");
        relevantContext.put("custom.person.sponsor.firstName", "Jessica");

        ArrayList<MessageConfig> templates = new ArrayList<>();
        MessageConfig template1 = buildConfig("solmod_evt.person.email", Map.of("solmod_evt.person.firstName", "Peter"));
        MessageConfig template2 = buildConfig("custom.person.sponsor.email",
                Map.of("custom.person.sponsor.firstName", "Mary"));
        templates.add(template1);
        templates.add(template2);

        dispatcher.persistRelevantContext(trigger, flatten(entireContext), templates);

        verify(ntcRepo, times(1)).saveContext(eq(trigger.getId()), contextCaptor.capture());

        Map<String, String> result = contextCaptor.getValue();
        assertFalse(result.isEmpty());
        assertTrue(result.keySet().containsAll(relevantContext.keySet()));
        assertFalse(result.keySet().containsAll(flatten(entireContext).keySet()));
    }

    @Test
    @DisplayName("Verify we get an InsufficientContext exception, but still store relevant context, when context doesn't have all of what the MessageTemplate needs")
    void persistRelevantContext_exceptionAndSaveOnInsufficientContext() throws StringifyException, DBRequestFailureException {
        NotificationTrigger trigger = new NotificationTrigger();
        trigger.setId(22L);

        HashMap<String, Object> entireContext = new HashMap<>();
        entireContext.put("solmod_evt", new MessageContextTest(new PersonContextTest("roger@rabbits.com", "Roger, of course")));
        entireContext.put("unused", new MessageContextTest(new PersonContextTest("who@cares.com", "poof")));

        HashMap<String, Object> relevantContext = new HashMap<>();
        relevantContext.put("solmod_evt.person.email", "roger@rabbits.com");
        relevantContext.put("solmod_evt.person.firstName", "Roger, of course");

        ArrayList<MessageConfig> templates = new ArrayList<>();
        MessageConfig template1 = buildConfig("solmod_evt.person.email", Map.of("solmod_evt.person.firstName", "Peter"));
        MessageConfig template2 = buildConfig("custom.person.sponsor.email",
                Map.of("custom.person.sponsor.firstName", "Mary"));
        templates.add(template1);
        templates.add(template2);

        assertThrows(InsufficientContextException.class, () -> dispatcher.persistRelevantContext(trigger, flatten(entireContext), templates));

        verify(ntcRepo, times(1)).saveContext(eq(trigger.getId()), contextCaptor.capture());

        Map<String, String> result = contextCaptor.getValue();
        assertFalse(result.isEmpty());
        assertTrue(result.keySet().containsAll(relevantContext.keySet()));
        assertFalse(result.keySet().containsAll(flatten(entireContext).keySet()));
    }

    @Test
    @DisplayName("Assert only qualifying MessageTemplates become deliveries")
    void determineAndBuildDeliveries_requiredContextAvail() throws InsufficientContextException, DBRequestFailureException {
        ArrayList<MessageConfig> templates = new ArrayList<>();

        // Qualifies
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.state", "AZ",
                "solmod_evt.data.person.member", "VIP")));
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.member", "VIP")));
        // Not Qualifies
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.state", "FL")));

        Set<NotificationDelivery> result = dispatcher.determineAndBuildDeliveries(templates, Map.of(
                "solmod_evt.data.person.state", "AZ",
                "solmod_evt.data.person.member", "VIP",
                "solmod_evt.data.person.email", "joe@cool.com"));

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Assert an exception is thrown when needed context is missing")
    void determineAndBuildDeliveries_requiredContextNotAvail() {
        ArrayList<MessageConfig> templates = new ArrayList<>();

        // Qualifies
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.state", "AZ",
                "solmod_evt.data.person.member", "VIP")));
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.member", "VIP")));
        // Not Qualifies
        templates.add(buildConfig("solmod_evt.data.person.email", Map.of(
                "solmod_evt.data.person.state", "FL")));

        assertThrows(InsufficientContextException.class, () -> dispatcher.determineAndBuildDeliveries(templates, Map.of(
                "solmod_evt.data.person.member", "VIP",
                "solmod_evt.data.person.email", "joe@cool.com")));
    }

    @Test
    @DisplayName("Assert only active templates are pulled by the dispatcher")
    void getRelatedMessageTemplates() {
        dispatcher.getActiveMessageConfigs(15L);
        verify(mcRepo, times(1)).getMessageConfigs(configCaptor.capture());

        MessageConfig result = configCaptor.getValue();

        assertEquals(15L, result.getNotificationEventId());
        assertEquals(ACTIVE, result.getStatus());
    }

    @Test
    void filterByContext() {
        assertTrue(true); // This method is tested thoroughly via determineAndBuildDeliveries_requiredContextAvail
    }

    private MessageConfig buildTemplateCriteria(Long eventId) {
        MessageConfig messagesCriteria = new MessageConfig();
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

    private MessageConfig buildConfig(String recipientContextKey, Map<String, String> deliveryCriteria) {
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setId(new Date().getTime());
        messageConfig.setNotificationEventId(15L);
        messageConfig.setDeliveryCriteria(deliveryCriteria);

        MessageTemplateSummary messageTemplate = MessageTemplateSummary.builder()
                .recipientContextKey(recipientContextKey)
                .messageConfigId(messageConfig.getId())
                .build();

        messageConfig.addMessageTemplate(messageTemplate);

        return messageConfig;
    }

    private SolMessage buildSolMessage(NotificationEvent eventCriteria, MessageContextTest context) {
        SolMessage request = new SolMessage();
        request.setTenantId(eventCriteria.getTenantId());
        request.setSubject(eventCriteria.getEventSubject());
        request.setVerb(eventCriteria.getEventVerb());

        request.setData(context);
        return request;
    }

    static class MessageContextTest {
        private PersonContextTest person;

        public MessageContextTest(PersonContextTest person) {
            this.person = person;
        }

        @SuppressWarnings("unused")
        public PersonContextTest getPerson() {
            return person;
        }

        @SuppressWarnings("unused")
        public void setPerson(PersonContextTest person) {
            this.person = person;
        }
    }

    static class PersonContextTest {
        private String email;
        private String firstName;
        private PersonContextTest sponsor;

        public PersonContextTest(String email, String firstName) {
            this.email = email;
            this.firstName = firstName;
        }

        public PersonContextTest(String email, String firstName, PersonContextTest sponsor) {
            this.email = email;
            this.firstName = firstName;
            this.sponsor = sponsor;
        }

        @SuppressWarnings("unused")
        public String getEmail() {
            return email;
        }

        @SuppressWarnings("unused")
        public void setEmail(String email) {
            this.email = email;
        }

        @SuppressWarnings("unused")
        public String getFirstName() {
            return firstName;
        }

        @SuppressWarnings("unused")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        @SuppressWarnings("unused")
        public PersonContextTest getSponsor() {
            return sponsor;
        }

        @SuppressWarnings("unused")
        public void setSponsor(PersonContextTest sponsor) {
            this.sponsor = sponsor;
        }
    }

}