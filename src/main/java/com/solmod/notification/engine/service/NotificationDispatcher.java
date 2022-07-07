package com.solmod.notification.engine.service;

import com.solmod.commons.StringifyException;
import com.solmod.notification.admin.data.MessageTemplatesRepository;
import com.solmod.notification.admin.data.NotificationEventsRepository;
import com.solmod.notification.admin.data.NotificationTriggersRepository;
import com.solmod.notification.domain.*;
import com.solmod.notification.exception.InsufficientContextException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

import static com.solmod.commons.StringUtils.bytesToHex;

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
     * Handler for messages on the SolBus
     * @param solMessage
     * @return
     */
    @Override
    public List<SolCommunication> apply(SolMessage solMessage) {
        log.info("Running NotificationDispatcher for {}:{}", solMessage.getSubject(), solMessage.getVerb());

        // 1. Find the appropriate event for the given message subject and verb
        NotificationEvent notificationEvent = getNotificationEvent(solMessage); // TODO: cache these
        if (notificationEvent == null) {
            return null; // No contexts found, so we can bail
        }

        try {
            NotificationTrigger trigger = logNotificationTrigger(notificationEvent);

            persistTemplateRelatedContext(trigger, solMessage);
        } catch (StringifyException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // TODO: handle this
        }

        // That should be the end of it. A Job should poll notification deliveries

        return null;
    }

    /**
     * <p>The context that is needed for a given NotificationEvent is determined by the MessageTemplates.
     * Message Templates need context for a number of different things, such as merge field in the message content, or
     * context based criteria.</p>
     * <p>So, this method will handle getting all MessageTemplates under the specified NotificationEvent and will build
     * an exhaustive list of the context properties needed to satisfy all references to context properties.</p>
     * <p><em>Since</em> we have all of this all loaded up here to do all that, then, we should trigger the delivery
     * of those MessageTemplates if the context contains all that's needed. Otherwise, this same thing has to happen
     * each time a callback from the CPI is received</p>
     * MessageTemplates need context data for the following reasons:
     * <ul>
     *     <li>Content merge fields</li>
     *     <li>Recipient</li>
     *     <li>Criteria</li>
     *     <li>Context builders</li>
     * </ul>
     *
     * @param trigger {@link NotificationTrigger}
     * @param eventMessage {@link SolMessage}
     * @throws InsufficientContextException The message is missing context params needed for properly evaluating
     * MessageTemplates, supplying needed merge fields...
     */
    void persistTemplateRelatedContext(NotificationTrigger trigger, SolMessage eventMessage)
            throws StringifyException {
        List<MessageTemplate> unfilteredTemplates = getRelatedMessageTemplates(trigger);

        NotificationContext context = new NotificationContext();
        context.addBuildContext("message", eventMessage);
        Set<NotificationDelivery> deliveries = new HashSet<>();

        // 4. For each qualifying MessageTemplate, create a Notification/Message delivery
        for (MessageTemplate messageTemplate : unfilteredTemplates) {
            NotificationDelivery delivery = new NotificationDelivery();
            delivery.setStatus(Status.PENDING_PERMISSION);

            try {
                boolean criteriaMatches = filterByContext(messageTemplate, context);
                if (!criteriaMatches) {
                    continue;
                }

                // 1. We may not have enough context for a recipient address. The resulting status must be PENDING_CONTEXT
                Object recipientAddy = context.getEventContext().get(messageTemplate.getRecipientContextKey());
                if (recipientAddy != null) {
                    delivery.setRecipient(recipientAddy.toString());
                } else {
                    throw new InsufficientContextException("Not enough context to determine recipient addy");
                }
            } catch (InsufficientContextException e) {
                log.warn(e.getMessage());
                delivery.setStatus(Status.PENDING_CONTEXT);

                // Future: we should proactively see if we can expect the expected context to be built by builders
            }

            delivery.setMessageTemplateId(messageTemplate.getId());
            delivery.setContext(context.getEventContext()); // TODO: Whittle this down to what's needed instead
            // Persist delivery

            deliveries.add(delivery);
        }

        if (deliveries.isEmpty()) {
            trigger.setStatus(Status.DELETED);
            try {
                ntRepo.update(trigger);
            } catch (NotificationTriggerNonexistentException e) {
                log.error("Somehow managed a missing NotificationTrigger when trying to update status to DELETED: {}", trigger );
            }
        }

        // TODO: Persist, relative to the NotificationDelivery, the context needed for the message templates that qualify
    }

    List<MessageTemplate> getRelatedMessageTemplates(NotificationTrigger trigger) {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setNotificationEventId(trigger.getNotificationEventId());
        criteria.setStatus(Status.ACTIVE);
        List<MessageTemplate> unfilteredTemplates = mtRepo.getMessageTemplates(criteria);
        return unfilteredTemplates;
    }


    boolean filterByContext(MessageTemplate messageTemplate, NotificationContext context)
        throws InsufficientContextException {
        if (messageTemplate.getDeliveryCriteria().isEmpty())
            return true;

        boolean meetsCriteria = true;
        for (Map.Entry<String, Object> criterion : messageTemplate.getDeliveryCriteria().entrySet()) {
            if (!context.getEventContext().containsKey(criterion.getKey())) {
                throw new InsufficientContextException("Can not filter Message Template. No value in context for " + criterion.getKey());
            }

            meetsCriteria = Objects.equals(context.getEventContext().get(criterion.getKey()), criterion.getValue());

            if (!meetsCriteria) {
                break;
            }
        }

        return meetsCriteria;
    }

    private NotificationTrigger logNotificationTrigger(NotificationEvent notificationEvent) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        NotificationTrigger trigger = new NotificationTrigger();
        trigger.setNotificationEventId(notificationEvent.getId());
        MessageDigest salt = MessageDigest.getInstance("SHA-256"); // TODO: extract this into a method away from here
        salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        String digest = bytesToHex(salt.digest());
        trigger.setUid(digest);
        // neRepo.create(trigger); TODO
        return trigger;
    }

    private NotificationEvent getNotificationEvent(SolMessage solMessage) {
        NotificationEvent ntSearchCriteria = new NotificationEvent();
        ntSearchCriteria.setStatus(Status.ACTIVE);
        ntSearchCriteria.setTenantId(solMessage.getTenantId());
        ntSearchCriteria.setEventVerb(solMessage.getVerb());
        ntSearchCriteria.setEventSubject(solMessage.getSubject());

        NotificationEvent notificationEvent = neRepo.getNotificationEvent(ntSearchCriteria);
        return notificationEvent;
    }

}
