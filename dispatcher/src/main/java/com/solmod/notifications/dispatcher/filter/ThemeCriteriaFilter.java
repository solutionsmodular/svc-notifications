package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ThemeCriteriaFilter  implements MessageDeliveryFilter {

    private final Logger log = LoggerFactory.getLogger(ThemeCriteriaFilter.class);

    @Override
    public FilterResponse apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        FilterResponse response = new FilterResponse("theme-criteria");
        if (templateGroup.getQualifiedTemplates().isEmpty()) {
            return response;
        }

        Map<String, Object> flattened = solMessage.buildMetadata();
        Set<MessageTemplate> messageTemplates = templateGroup.getQualifiedTemplates();
        for (MessageTemplateDTO curTemplate : messageTemplates) { // use iter.hasNext because of iter.remove used herein
            response.addDeliveryPermission(curTemplate.getMessageTemplateID(), qualifyTemplate(curTemplate, flattened) ?
                    DeliveryPermission.SEND_NOW : DeliveryPermission.SEND_NEVER);
        }

        return response;
    }

    private boolean qualifyTemplate(MessageTemplateDTO curTemplate, Map<String, Object> flattenedMetadata) {
        if (curTemplate.getDeliveryCriteria() == null || curTemplate.getDeliveryCriteria().getCriteria().isEmpty()) {
            return true;
        }

        AtomicBoolean qualifies = new AtomicBoolean(true);
        curTemplate.getDeliveryCriteria().getCriteria().forEach((key, value) -> {
            Object propertyValue = flattenedMetadata.get(key);
            if (!Objects.equals(propertyValue, value)) {
                qualifies.set(false);
                log.info("Metadata {} {}", key, propertyValue == null ? "missing a value" : "has invalid value");
            }
        });

        return qualifies.get();
    }
}
