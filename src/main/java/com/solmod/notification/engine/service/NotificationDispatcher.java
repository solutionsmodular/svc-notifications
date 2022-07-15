package com.solmod.notification.engine.service;

import com.solmod.commons.StringifyException;
import com.solmod.notification.admin.data.*;
import com.solmod.notification.domain.*;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.InsufficientContextException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

import static com.solmod.commons.ObjectUtils.flatten;
import static com.solmod.notification.admin.data.DataUtils.generateUid;

@Service("NotificationDispatcher")
public class NotificationDispatcher implements Function<SolMessage, List<SolCommunication>> {

    NotificationEventsRepository neRepo;
    MessageTemplatesRepository mtRepo;
    NotificationTriggersRepository ntRepo;
    NotificationTriggerContextRepository ntcRepo;
    NotificationDeliveriesRepository ndRepo;

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationDispatcher(NotificationEventsRepository neRepo,
                                  MessageTemplatesRepository mtRepo,
                                  NotificationTriggersRepository ntRepo,
                                  NotificationTriggerContextRepository ntcRepo,
                                  NotificationDeliveriesRepository ndRepo) {
        this.neRepo = neRepo;
        this.mtRepo = mtRepo;
        this.ntRepo = ntRepo;
        this.ntcRepo = ntcRepo;
        this.ndRepo = ndRepo;
    }

    /**
     * Message Bus Subscriber
     * This signature accepts messages from the message bus which may trigger a notification. Assumes no context
     * already exists for the given trigger.
     *
     * @param solMessage {@link SolMessage} event message from off the bus
     * @return List of {@link SolCommunication}s suited to send to the sender
     */
    @Override
    public List<SolCommunication> apply(SolMessage solMessage) {

        // Find the appropriate event for the given message subject and verb
        NotificationEvent notificationEvent =
                getNotificationEvent(solMessage.getTenantId(), solMessage.getSubject(), solMessage.getVerb());
        if (notificationEvent == null) {
            return null; // No notifications configured for this event, so we can bail
        }

        log.info("Running NotificationDispatcher for {}:{}", solMessage.getSubject(), solMessage.getVerb());

        NotificationTrigger trigger = null;

        try {

            try {
                List<MessageTemplate> allEventTemplates = getRelatedMessageTemplates(notificationEvent.getId());
                if (allEventTemplates.isEmpty()) {
                    log.warn("NotificationEvent with no MessageTemplates: {}:{}. Should it be disabled??", notificationEvent.getEventSubject(), notificationEvent.getEventVerb());
                    return Collections.emptyList();
                }

                trigger = logNotificationTrigger(notificationEvent);

                Map<String, Object> context = flatten(Map.of("message", solMessage));
                Map<String, Object> relevantContext = persistRelevantContext(trigger, context, allEventTemplates);

                // Should only be here if all needed context exists
                Set<NotificationDelivery> notificationDeliveries = determineAndBuildDeliveries(allEventTemplates, relevantContext);
                if (!notificationDeliveries.isEmpty()) {
                    processDeliveries(trigger, notificationDeliveries);
                }
            } catch (InsufficientContextException e) {
                log.info("More context is needed to process the event {}:{}. This event has {} context builders.:\n{}",
                        notificationEvent.getEventSubject(), notificationEvent.getEventVerb(), 0, e.getMessage());
                trigger.setStatus(Status.PENDING_CONTEXT);
                ntRepo.update(trigger);
            }
        } catch (NoSuchAlgorithmException | StringifyException | DBRequestFailureException e) {
            log.error("FATAL: {} when trying to handle event message {} | NotificationEvent: {}",
                    e.getMessage(), solMessage, trigger);
        }

        return null;
    }

    private void processDeliveries(NotificationTrigger trigger, Set<NotificationDelivery> notificationDeliveries) throws DBRequestFailureException {
        log.debug("Processing {} deliveries for trigger {}", notificationDeliveries.size(), trigger.getId());

        for (NotificationDelivery delivery : notificationDeliveries) {
            // TODO: Submit delivery request to CPI
            delivery.setDeliveryProcessKey("what-comes-back-from-CPI");
            ndRepo.create(delivery);
        }

        trigger.setStatus(Status.PENDING_DELIVERY);
        ntRepo.update(trigger);
    }

    /**
     * <p>From whatever context has been built in the supplied {@link NotificationTrigger} - which is presumed to be
     * pre-filtered per its relevance to filtering and processing qualifying templates - store the context which
     * is relevant to processing {@link MessageTemplate}s subscribing to the {@link NotificationEvent} triggered.</p>
     * <p>The context that is needed for a given NotificationEvent is determined by the MessageTemplates.
     * Message Templates need context for a number of different things, such as merge field in the message content, or
     * context based criteria.</p>
     * <p>So, this method will handle getting all MessageTemplates under the specified NotificationEvent and will build
     * and return an exhaustive list of the context properties needed to satisfy all references to context properties.
     * </p>
     * <p><em>NOTE</em>: context is handled separate from trigger because this is the method that isolates the relevant
     * properties from the entire context, and that will be set to the trigger
     *</p>
     *
     * @param trigger             {@link NotificationTrigger}
     * @param context             Map<String, Object>
     * @param qualifyingTemplates List of {@link MessageTemplate}s predetermined will be sent
     * @return Context Map containing the context properties relevant to the {@link MessageTemplate}s
     * @throws InsufficientContextException If the given Context Map is missing fields needed to process
     *                                      {@link MessageTemplate}s
     */
    Map<String, Object> persistRelevantContext(NotificationTrigger trigger,
                                               Map<String, Object> context,
                                               List<MessageTemplate> qualifyingTemplates)
            throws InsufficientContextException, DBRequestFailureException {

        Set<String> propertiesNeeded = new HashSet<>();

        for (MessageTemplate qualifyingTemplate : qualifyingTemplates) {
            propertiesNeeded.add(qualifyingTemplate.getRecipientContextKey());
            propertiesNeeded.addAll(qualifyingTemplate.getDeliveryCriteria().keySet());
            // TODO: propertiesNeeded.addAll(cms.getMergeFields())
        }

        HashMap<String, Object> relevantContext = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder();
        for (String neededProp : propertiesNeeded) {
            if (context.get(neededProp) != null) {
                relevantContext.put(neededProp, context.get(neededProp));
            } else {
                errorMessage.append(errorMessage.length() > 0 ? ", " : "").append(neededProp);
            }
        }

        if (!relevantContext.isEmpty()) {
            ntcRepo.create(trigger.getId(), relevantContext);
        }

        // Now, ensure the keys in context contain all that are needed
        if (errorMessage.length() > 0) {
            throw new InsufficientContextException(String.format(
                    "InsufficientContext: Templates for NotificationTrigger %s missing the following context: %s",
                    trigger.getId(), errorMessage));
        }

        return relevantContext;
    }

    /**
     * Helper to build {@link NotificationDelivery}s for the templates associated with a triggered {@link NotificationEvent}
     *
     * @param templates       List of {@link MessageTemplate}s for which to initialize deliveries
     * @param relevantContext Context needed for processing any part of the {@link MessageTemplate}s
     * @return Set of {@link NotificationDelivery}s encapsulating what should be delivered given the params supplied
     */
    Set<NotificationDelivery> determineAndBuildDeliveries(List<MessageTemplate> templates, Map<String, Object> relevantContext)
            throws InsufficientContextException, DBRequestFailureException {

        Set<NotificationDelivery> deliveries = new HashSet<>();

        for (MessageTemplate messageTemplate : templates) {
            //
            // DETERMINE
            NotificationDelivery delivery = new NotificationDelivery();
            delivery.setStatus(Status.PENDING_PERMISSION);

            // filterByContext will throw an exception if there's not enough context
            boolean criteriaMatches = filterByContext(messageTemplate, relevantContext);
            if (!criteriaMatches) {
                continue;
            }

            //
            // BUILD
            Object recipient = relevantContext.get(messageTemplate.getRecipientContextKey());
            // Per orig design, this shouldn't be null, but could actually be blank, which would be bad
            if (recipient != null && !StringUtils.isBlank(recipient.toString())) {
                delivery.setRecipient(recipient.toString());
            } else {
                throw new InsufficientContextException("Not enough context to determine recipient addy");
            }

            delivery.setMessageTemplateId(messageTemplate.getId());
            // TODO: Get merged content, send to S3, associate that endpoint
            if (deliveries.isEmpty())
                delivery.setMessageBodyUri("Helloooooo");

            NotificationDelivery another = new NotificationDelivery();
            another.setMessageBodyUri("what's up");
            another.setDeliveryProcessKey("another thing");

            deliveries.add(delivery);
        }

        return deliveries;
    }

    /**
     * Helper method to get all active MessageTemplates that apply to the given NotificationTrigger
     *
     * @param notificationEventId The {@link NotificationEvent} for which to retrieve all MessageTemplates
     * @return List of {@link MessageTemplate}s which subscribe to the given {@link NotificationTrigger}
     */

    List<MessageTemplate> getRelatedMessageTemplates(Long notificationEventId) {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setNotificationEventId(notificationEventId);
        criteria.setStatus(Status.ACTIVE);
        return mtRepo.getMessageTemplates(criteria);
    }


    boolean filterByContext(MessageTemplate messageTemplate, Map<String, Object> context)
            throws InsufficientContextException {
        if (messageTemplate.getDeliveryCriteria().isEmpty())
            return true;

        boolean meetsCriteria = true;
        for (Map.Entry<String, String> criterion : messageTemplate.getDeliveryCriteria().entrySet()) {
            if (!context.containsKey(criterion.getKey())) {
                throw new InsufficientContextException("Can not filter Message Template. No value in context for " + criterion.getKey());
            }

            meetsCriteria = Objects.equals(context.get(criterion.getKey()), criterion.getValue());

            if (!meetsCriteria) {
                break;
            }
        }

        return meetsCriteria;
    }

    private NotificationTrigger logNotificationTrigger(NotificationEvent notificationEvent)
            throws NoSuchAlgorithmException {
        String uid = generateUid();

        NotificationTrigger trigger = new NotificationTrigger();
        trigger.setNotificationEventId(notificationEvent.getId());
        trigger.setUid(uid);
        trigger.setStatus(Status.NO_OP); // For now...

        try {
            trigger.setId(ntRepo.create(trigger));
        } catch (DBRequestFailureException e) {
            log.error("Failure attempting to logNotificationTrigger! A whole event is LOST!", e);
        }

        return trigger;
    }

    private NotificationEvent getNotificationEvent(Long tenantId, String subject, String verb) {
        NotificationEvent ntSearchCriteria = new NotificationEvent();
        ntSearchCriteria.setStatus(Status.ACTIVE);
        ntSearchCriteria.setTenantId(tenantId);
        ntSearchCriteria.setEventSubject(subject);
        ntSearchCriteria.setEventVerb(verb);

        return neRepo.getNotificationEvent(ntSearchCriteria);
    }

}
