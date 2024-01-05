package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.web.model.MessageTemplateGroup;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationAccessService {

    NotificationGroupRepo groupRepo;

    @Autowired
    public NotificationAccessService(NotificationGroupRepo groupRepo) {
        this.groupRepo = groupRepo;
    }

    public MessageTemplateGroup getNotificationTemplateGroup(Long tenantId, @NotNull String subject, @NotNull String verb) {
        groupRepo.findByTenantIdAndSubjectAndVerb(tenantId, subject, verb);
        return null;
    }
}
