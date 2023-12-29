package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.UUID;

@Entity
public class MessageTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToMany(mappedBy = "theme")
    private Collection<EventCriteria> criteria;
    @ManyToOne(fetch = FetchType.LAZY)
    private NotificationGroup notificationGroup;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Collection<EventCriteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(Collection<EventCriteria> criteria) {
        this.criteria = criteria;
    }

    public NotificationGroup getNotificationGroup() {
        return notificationGroup;
    }

    public void setNotificationGroup(NotificationGroup notificationGroup) {
        this.notificationGroup = notificationGroup;
    }
}
