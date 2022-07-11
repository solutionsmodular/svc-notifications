package com.solmod.notification.engine.service;

import com.solmod.commons.StringifyException;
import com.solmod.notification.admin.data.MessageTemplatesRepository;
import com.solmod.notification.admin.data.NotificationEventsRepository;
import com.solmod.notification.admin.data.NotificationTriggersRepository;
import com.solmod.notification.domain.*;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.InsufficientContextException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

import static com.solmod.commons.ObjectUtils.flatten;
import static com.solmod.commons.SolStringUtils.bytesToHex;

@Service("NotificationDispatcher")
public class NotificationDispatcher implements Function<SolMessage, List<SolCommunication>> {

    NotificationEventsRepository neRepo;
    MessageTemplatesRepository mtRepo;
    NotificationTriggersRepository ntRepo;

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationDispatcher(MessageTemplatesRepository mtRepo) {
        this.mtRepo = mtRepo;
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
        NotificationEvent notificationEvent = getNotificationEvent(solMessage);
        if (notificationEvent == null) {
            return null; // No notifications configured for this event, so we can bail
        }

        log.info("Running NotificationDispatcher for {}:{}", solMessage.getSubject(), solMessage.getVerb());

        NotificationTrigger trigger = null;

        try {
            trigger = logNotificationTrigger(notificationEvent);

            try {
                List<MessageTemplate> allEventTemplates = getRelatedMessageTemplates(trigger);
                if (allEventTemplates.isEmpty()) {
                    log.warn("NotificationEvent with no MessageTemplates: {}:{}. Should it be disabled??", notificationEvent.getEventSubject(), notificationEvent.getEventVerb());

                    trigger.setStatus(Status.DELETED);
                    ntRepo.update(trigger);
                    return Collections.emptyList();
                }

                Map<String, Object> context = flatten(solMessage);
                Map<String, Object> relevantContext = persistRelevantContext(trigger, context, allEventTemplates);

                // Should only be here if all needed context exists
                Set<NotificationDelivery> notificationDeliveries = determineAndBuildDeliveries(allEventTemplates, relevantContext);
                log.debug("Found {} deliveries for the given trigger", notificationDeliveries.size());
                // TODO: send deliveries to be delivered
            } catch (InsufficientContextException e) {
                trigger.setStatus(Status.PENDING_CONTEXT);
                ntRepo.update(trigger);
            }
        } catch (NoSuchAlgorithmException | StringifyException e) {
            // TODO: handle this
        } catch (NotificationTriggerNonexistentException e) {
            log.error("Somehow managed a missing NotificationTrigger when trying to update status: {}", trigger);
        }

        // That should be the end of it. A Job should poll notification deliveries

        return null;
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
            throws InsufficientContextException {

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

        // TODO: batch save whatever relevant context we have for the trigger

        // Now, ensure the keys in context contain all that are needed
        if (errorMessage.length() > 0) {
            throw new InsufficientContextException(String.format(
                    "InsufficientContext: Templates for NotificationTrigger %s missing the following context: %s",
                    trigger.getId(), errorMessage));
        }

        return relevantContext;
    }

    /**
     * @param templates       List of {@link MessageTemplate}s for which to initialize deliveries
     * @param relevantContext Context needed for processing any part of the {@link MessageTemplate}s
     * @return Set of {@link NotificationDelivery}s encapsulating what should be delivered given the params supplied
     */
    Set<NotificationDelivery> determineAndBuildDeliveries(List<MessageTemplate> templates, Map<String, Object> relevantContext)
            throws InsufficientContextException {

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
            if (recipient != null && StringUtils.isBlank(recipient.toString())) {
                delivery.setRecipient(recipient.toString());
            } else {
                throw new InsufficientContextException("Not enough context to determine recipient addy");
            }

            delivery.setMessageTemplateId(messageTemplate.getId());
            // TODO: Persist delivery. In persist, could generate uid so we don't have to go get it
            deliveries.add(delivery);
        }

        return deliveries;
    }

    /**
     * Helper method to get all active MessageTemplates that apply to the given NotificationTrigger
     *
     * @param trigger {@link NotificationTrigger} Instance of an event for which there is a notification configured
     * @return List of {@link MessageTemplate}s which subscribe to the given {@link NotificationTrigger}
     */

    List<MessageTemplate> getRelatedMessageTemplates(NotificationTrigger trigger) {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setNotificationEventId(trigger.getNotificationEventId());
        criteria.setStatus(Status.ACTIVE);
        return mtRepo.getMessageTemplates(criteria);
    }


    boolean filterByContext(MessageTemplate messageTemplate, Map<String, Object> context)
            throws InsufficientContextException {
        if (messageTemplate.getDeliveryCriteria().isEmpty())
            return true;

        boolean meetsCriteria = true;
        for (Map.Entry<String, Object> criterion : messageTemplate.getDeliveryCriteria().entrySet()) {
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

    private String generateUid() throws NoSuchAlgorithmException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256"); // TODO: extract this into a method away from here
        salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        return bytesToHex(salt.digest());
    }

    private NotificationEvent getNotificationEvent(SolMessage solMessage) {
        NotificationEvent ntSearchCriteria = new NotificationEvent();
        ntSearchCriteria.setStatus(Status.ACTIVE);
        ntSearchCriteria.setTenantId(solMessage.getTenantId());
        ntSearchCriteria.setEventVerb(solMessage.getVerb());
        ntSearchCriteria.setEventSubject(solMessage.getSubject());

        return neRepo.getNotificationEvent(ntSearchCriteria);
    }

}
