package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.NotificationGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface NotificationGroupRepo extends CrudRepository<NotificationGroup, UUID> {
}
