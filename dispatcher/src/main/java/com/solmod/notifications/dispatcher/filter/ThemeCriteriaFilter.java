package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ThemeCriteriaFilter  implements MessageDeliveryFilter {

    @Override
    public FilterResponse apply(TriggeredMessageTemplateGroup templateGroup, TriggeringEvent trigger) {
        FilterResponse response = new FilterResponse("theme-criteria");
        if (templateGroup.getQualifiedTemplates().isEmpty()) {
            return response;
        }

        Map<String, String> flattened = trigger.getEventMetadata();
        Set<MessageTemplate> messageTemplates = templateGroup.getQualifiedTemplates();
        for (MessageTemplateDTO curTemplate : messageTemplates) { // use iter.hasNext because of iter.remove used herein
            response.addDeliveryPermission(curTemplate.getMessageTemplateID(), qualifyTemplate(curTemplate, flattened));
        }

        return response;
    }

    private DeliveryPermission qualifyTemplate(MessageTemplateDTO curTemplate, Map<String, String> flattenedMetadata) {
        if (curTemplate.getDeliveryCriteria() == null || curTemplate.getDeliveryCriteria().getCriteria().isEmpty()) {
            return DeliveryPermission.SEND_NOW_PERMISSION;
        }

        for (Map.Entry<String, String> s : curTemplate.getDeliveryCriteria().getCriteria().entrySet()) {
            Object metadataValue = flattenedMetadata.get(s.getKey());
            if (metadataValue == null) {
                return new DeliveryPermission(DeliveryPermission.Verdict.SEND_NEVER, "Message metadata missing template criterion " + s.getKey());
            }
            if (!metadataValue.toString().equals(s.getValue())) {
                return new DeliveryPermission(DeliveryPermission.Verdict.SEND_NEVER, "Message metadata has incorrect value for template criterion " + s.getKey());
            }
        }

        return DeliveryPermission.SEND_NOW_PERMISSION;
    }
}
