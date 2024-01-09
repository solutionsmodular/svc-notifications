package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.NotificationGroup;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationGroupRepo extends CrudRepository<NotificationGroup, Long> {

    NotificationGroup findByTenantIdAndSubjectAndVerb(@NotNull Long tenantId, @NotNull String subject, @NotNull String verb);
}
