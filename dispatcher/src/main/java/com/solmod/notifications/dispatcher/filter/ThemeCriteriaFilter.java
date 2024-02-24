package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ThemeCriteriaFilter  implements MessageDeliveryFilter {

    @Override
    public String getFilterName() {
        return "theme-criteria";
    }

    @Override
    public DeliveryPermission apply(MessageTemplate messageTemplate, TriggeringEvent trigger) {
        Map<String, String> flattened = trigger.getEventMetadata();
        return qualifyTemplate(messageTemplate, flattened);
    }

    private DeliveryPermission qualifyTemplate(MessageTemplate curTemplate, Map<String, String> flattenedMetadata) {
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
