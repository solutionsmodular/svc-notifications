package com.solmod.notification.engine.service;

import com.solmod.notification.admin.data.MessageTemplatesRepository;
import com.solmod.notification.domain.SolCommunication;
import com.solmod.notification.domain.SolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service("NotificationDispatcher")
public class NotificationDispatcher implements Function<SolMessage, List<SolCommunication>> {

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

        // 2. If one is found, then run its builders

        // 3. With context, search for Message Templates for this Notification Context, filtering by any criteria

        // 4. For each qualifying MessageTemplate, create a Notification/Message delivery

        // 5. For each Message Template, get the content, logging an error those that have merge-fields not in context

        // 6. Begin delivery process for Templates which do not error
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
}
