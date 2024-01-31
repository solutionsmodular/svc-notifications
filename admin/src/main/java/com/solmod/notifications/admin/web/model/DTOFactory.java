package com.solmod.notifications.admin.web.model;

import com.solmod.notifications.admin.repository.model.MessageTemplate;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

public class DTOFactory {

    /**
     * Create a {@link MessageTemplateDTO} of the {@link NotificationGroup} provided
     *
     * @param entity {@link NotificationGroup}
     * @return {@link MessageTemplateDTO}
     */
    public static MessageTemplateGroupDTO fromEntity(NotificationGroup entity) {
        Set<MessageTemplateDTO> messageTemplates = new HashSet<>();
        MessageTemplateGroupDTO result = new MessageTemplateGroupDTO();
        for (Theme curThemeEntity : entity.getThemes()) {
            Collection<MessageTemplate> templateEntities = curThemeEntity.getMessageTemplates();
            for (MessageTemplate curTemplateEntity : templateEntities) {
                MessageTemplateDTO templateDTO = templateFromEntity(curThemeEntity, curTemplateEntity);
                messageTemplates.add(templateDTO);
            }
        }

        result.setMessageTemplates(messageTemplates);
        return result;
    }

    public static MessageTemplateDTO templateFromEntity(Theme themeEntity, MessageTemplate templateEntity) {
        MessageTemplateDTO templateDTO = new MessageTemplateDTO();
        templateDTO.setDeliveryCriteria(buildCriteriaSet(themeEntity.getCriteria()));
        templateDTO.setMessageTemplateID(templateEntity.getId());
        templateDTO.setContentKeySet(templateEntity.toContentKeySet());
        templateDTO.setRecipientAddressContextKey(templateEntity.getRecipientAddressContextKey());
        templateDTO.setSender(templateEntity.getSender());
        templateDTO.setMinWaitForRetry(templateEntity.getMinWaitForRetry());
        templateDTO.setMaxRetries(templateEntity.getMaxRetries());
        templateDTO.setMessageClass(templateEntity.getMessageClass().name());
        // Limit to the lowest value between Theme and Template

        Integer resultMaxSend = null;
        if (templateEntity.getMaxSend() != null && themeEntity.getMaxSend() != null) {
            resultMaxSend = min(templateEntity.getMaxSend(), themeEntity.getMaxSend());
        } else if (templateEntity.getMaxSend() != null) {
            resultMaxSend = templateEntity.getMaxSend();
        } else if (themeEntity.getMaxSend() != null) {
            resultMaxSend = themeEntity.getMaxSend();
        }

        templateDTO.setMaxSend(resultMaxSend);
        // Use the greatest configured value for interval between Theme and Template
        templateDTO.setResendInterval(max(templateEntity.getResendInterval(), themeEntity.getResendInterval()));
        return templateDTO;
    }

    private static DeliveryCriterionSetDTO buildCriteriaSet(Collection<ThemeCriteria> criteria) {
        DeliveryCriterionSetDTO result = new DeliveryCriterionSetDTO();
        for (ThemeCriteria criterion : criteria) {
            result.addCriterion(criterion.getKey(), criterion.getValue());
        }

        return result;
    }
}
