package com.solmod.notification.engine.service;

import com.solmod.notification.admin.data.MessageTemplateSearchCriteria;
import com.solmod.notification.engine.data.NotificationEngineRepository;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.SolCommunication;
import com.solmod.notification.domain.SolMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Service("NotificationDispatcher")
public class NotificationDispatcher implements Function<SolMessage, List<SolCommunication>> {

    NotificationEngineRepository mtRepo;

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationDispatcher(NotificationEngineRepository mtRepo) {
        this.mtRepo = mtRepo;
    }

    @Override
    public List<SolCommunication> apply(SolMessage solMessage) {
        log.info("Running NotificationDispatcher for {}:{}", solMessage.getSubject(), solMessage.getVerb());

        MessageTemplateSearchCriteria crit = new MessageTemplateSearchCriteria();
        crit.setEventSubject(solMessage.getSubject());
        crit.setEventVerb(solMessage.getVerb());
        crit.setTenantId(solMessage.getTenantId());
        crit.setContext(solMessage.getData());

        Collection<MessageTemplate> messageTemplates = mtRepo.getMessageTemplates(crit);

        // For ea template:
        //   Gather delivery criteria

        return null;
    }
}
