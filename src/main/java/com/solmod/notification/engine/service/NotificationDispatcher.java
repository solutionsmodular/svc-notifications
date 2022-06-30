package com.solmod.notification.engine.service;

import com.solmod.notification.admin.data.MessageTemplatesRepository;
import com.solmod.notification.admin.data.NotificationContextRepository;
import com.solmod.notification.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service("NotificationDispatcher")
public class NotificationDispatcher implements Function<SolMessage, List<SolCommunication>> {

    NotificationContextRepository ncRepo;
    MessageTemplatesRepository mtRepo;

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationDispatcher(MessageTemplatesRepository mtRepo) {
        this.mtRepo = mtRepo;
    }

    @Override
    public List<SolCommunication> apply(SolMessage solMessage) {
        log.info("Running NotificationDispatcher for {}:{}", solMessage.getSubject(), solMessage.getVerb());

        // 1. Find the appropriate context for the given message subject and verb
        NotificationEvent notificationEvent = getApplicableNotificationContext(solMessage);
        if (notificationEvent == null) {
            return null; // No contexts found, so we can bail
        }

        // 2. If one is found, then run its builders
        NotificationContext context = buildContext(notificationEvent, solMessage);

        // 3. With context, search for Message Templates for this Notification Context, filtering by any criteria
        Set<MessageTemplate> messageTemplates = mtRepo.getMessageTemplates(notificationEvent, solMessage);

        // 4. For each qualifying MessageTemplate, create a Notification/Message delivery
        for (MessageTemplate messageTemplate : messageTemplates) {
            NotificationDelivery delivery = new NotificationDelivery();
            delivery.setStatus(Status.PENDING);
            delivery.setRecipient(context.getBuiltContext().get(messageTemplate.getRecipientContextKey()));
            delivery.setMessageTemplateId(messageTemplate.getId());
            delivery.setContext(context.getMinContext());
            // Persist delivery

            // buildMessageContent - logging error on missing merge-fields
            // Save message body to S3
            delivery.setMessageBodyUri("S3-addy");
            // Persist delivery
        }

        // That should be the end of it. A Job should poll notification deliveries
/*
        MessageTemplate crit = new MessageTemplate();
        crit.setEventSubject(solMessage.getSubject());
        crit.setEventVerb(solMessage.getVerb());
        crit.setTenantId(solMessage.getTenantId());
        crit.setContext(solMessage.getData());

        Collection<MessageTemplate> messageTemplates = mtRepo.getMessageTemplates(crit);

        for (MessageTemplate messageTemplate : messageTemplates) {
            Map<String, Object> dCriteria = messageTemplate.getDeliveryCriteria(); // Loaded
            List<MessageChannel> messageChannels = new ArrayList<>();
            for (MessageChannel messageChannel : messageChannels) {
                // For ea channel, get the content
            }

        }
*/

        // For ea template:
        //   Gather delivery criteria

        return null;
    }

    /**
     * Some day, this will be extracted into the CPI
     * Take the ContextBuilders for the given NotificationEvent and run them to build the context
     *
     * @param notificationEvent {@link NotificationEvent}
     * @param solMessage {@link SolMessage} triggering a notification
     * @return Context Map
     */
    private NotificationContext buildContext(NotificationEvent notificationEvent, SolMessage solMessage) {
        return null;
    }

    private NotificationEvent getApplicableNotificationContext(SolMessage solMessage) {
        NotificationEvent ntSearchCriteria = new NotificationEvent();
        ntSearchCriteria.setStatus(Status.ACTIVE);
        ntSearchCriteria.setTenantId(solMessage.getTenantId());
        ntSearchCriteria.setEventVerb(solMessage.getVerb());
        ntSearchCriteria.setEventSubject(solMessage.getSubject());
        NotificationEvent notificationEvent = ncRepo.getNotificationContext(ntSearchCriteria);
        return notificationEvent;
    }
}
