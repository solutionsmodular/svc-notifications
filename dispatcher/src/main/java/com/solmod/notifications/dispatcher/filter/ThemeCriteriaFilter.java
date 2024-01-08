package com.solmod.notifications.dispatcher.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ThemeCriteriaFilter  implements MessageDeliveryFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(ThemeCriteriaFilter.class);

    @Override
    public void apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        try {
            Map<String, Object> flattened = flatten(solMessage.getData());
            Set<MessageTemplateDTO> messageTemplates = templateGroup.getQualifiedTemplates().getMessageTemplates();
            Iterator<MessageTemplateDTO> templateIter = messageTemplates.iterator();
            while (templateIter.hasNext()) { // use iter.hasNext because of iter.remove used herein
                MessageTemplateDTO curTemplate = templateIter.next();
                if (!qualifyTemplate(curTemplate, flattened)) {
                    templateIter.remove();
                    templateGroup.addDenyMessage(curTemplate.getMessageTemplateID(), "Criteria not met");
                    log.info("Rejecting template due to missing or mis-matched criterion");
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
            }
        });

        return qualifies.get();
    }

    /**
     * Create a flat, Properties-like, construct representing the data in the provided context. This facilitates
     * the use of context keys such as {@code parent.child.property}
     *
     * @param context {@code Object} of any sort, to flatten
     * @return Map of String key Object value context
     * @throws JsonProcessingException In the event there's something funky with the specified context
     */
    public Map<String, Object> flatten(Object context) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(context);

        return JsonFlattener.flattenAsMap(json);
    }
}
