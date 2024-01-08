package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.repository.model.MessageTemplate;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

@Service
public class NotificationAccessService {

    NotificationGroupRepo groupRepo;

    @Autowired
    public NotificationAccessService(NotificationGroupRepo groupRepo) {
        this.groupRepo = groupRepo;
    }

    @Transactional
    public MessageTemplateGroupDTO getNotificationTemplateGroup(Long tenantId, @NotNull String subject, @NotNull String verb) {
        NotificationGroup templateGroup = groupRepo.findByTenantIdAndSubjectAndVerb(tenantId, subject, verb);

        return DTOFactory.fromEntity(templateGroup);
    }

    private static class DTOFactory {

        /**
         * Create a {@link MessageTemplateDTO} of the {@link NotificationGroup} provided
         *
         * @param entity {@link NotificationGroup}
         * @return {@link MessageTemplateDTO}
         */
        private static MessageTemplateGroupDTO fromEntity(NotificationGroup entity) {
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

        private static MessageTemplateDTO templateFromEntity(Theme themeEntity, MessageTemplate templateEntity) {
            MessageTemplateDTO templateDTO = new MessageTemplateDTO();
            templateDTO.setDeliveryCriteria(buildCriteriaSet(themeEntity.getCriteria()));
            templateDTO.setMessageTemplateID(templateEntity.getId());
            templateDTO.setContentKeySet(templateEntity.toContentKeySet());
            templateDTO.setRecipientAddressContextKey(templateEntity.getRecipientAddressContextKey());
            templateDTO.setSender(templateEntity.getSender());
            templateDTO.setMinWaitForRetry(templateEntity.getMinWaitForRetry());
            templateDTO.setMaxRetries(templateEntity.getMaxRetries());
            // Limit to the lowest value between Theme and Template
            templateDTO.setMaxSend(min(templateEntity.getMaxSend(), themeEntity.getMaxSend()));
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

}

