package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.web.model.DTOFactory;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}

