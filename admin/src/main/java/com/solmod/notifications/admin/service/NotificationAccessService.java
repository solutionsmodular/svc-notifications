package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.repository.model.MessageTemplate;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.web.model.DeliveryCriteriaDTO;
import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

        MessageTemplateGroupDTO result = DTOFactory.fromEntity(templateGroup);

        return result;
    }

    private class DTOFactory {

        private static MessageTemplateGroupDTO fromEntity(NotificationGroup entity) {
            HashMap<DeliveryCriterionSetDTO, Set<MessageTemplateDTO>> messageTemplates = new HashMap<>();
            MessageTemplateGroupDTO result = new MessageTemplateGroupDTO();
            for (Theme curThemeEntity : entity.getThemes()) {
                DeliveryCriterionSetDTO criteriaSet = buildCriteriaSet(curThemeEntity.getCriteria());
                Set<MessageTemplateDTO> keyedMessageTemplates = messageTemplates.computeIfAbsent(criteriaSet, k -> new HashSet<>());
                Collection<MessageTemplate> templateEntities = curThemeEntity.getMessageTemplates();
                for (MessageTemplate curTemplateEntity : templateEntities) {
                    MessageTemplateDTO templateDTO = initializeTemplate(curThemeEntity);
                    templateDTO.setMaxRetries(curTemplateEntity.getMaxRetries());
                    templateDTO.setMaxSend(curTemplateEntity.getMaxSend()); // TODO: min
                    templateDTO.setResendInterval(curTemplateEntity.getResendInterval()); // TODO: min
                    templateDTO.setMessageBodyContentKey(curTemplateEntity.getMessageBodyContentKey());
                    templateDTO.setRecipientAddressContextKey(curTemplateEntity.getRecipientAddressContextKey());
                    templateDTO.setSender(curTemplateEntity.getSender());
                    templateDTO.setMinWaitForRetry(curTemplateEntity.getMinWaitForRetry());
                    templateDTO.setResendIntervalPeriod(curTemplateEntity.getResendIntervalPeriod());

                    keyedMessageTemplates.add(templateDTO);
                }

                messageTemplates.put(criteriaSet, keyedMessageTemplates);
            }

            result.setMessageTemplates(messageTemplates);
            return result;
        }

        private static DeliveryCriterionSetDTO buildCriteriaSet(Collection<ThemeCriteria> criteria) {
            DeliveryCriterionSetDTO result = new DeliveryCriterionSetDTO();
            for (ThemeCriteria criterion : criteria) {
                result.addCriterion(new DeliveryCriteriaDTO(criterion.getKey(), criterion.getValue()));
            }

            return result;
        }

        private static MessageTemplateDTO initializeTemplate(Theme curThemeEntity) {
            MessageTemplateDTO result = new MessageTemplateDTO();
            // this is part of the collection above:  curThemeEntity.getCriteria();

            result.setResendIntervalPeriod(curThemeEntity.getResendIntervalPeriod());
            result.setMaxSend(curThemeEntity.getMaxSend());
            return result;
        }
    }

}

