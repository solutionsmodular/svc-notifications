package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "MessageTheme")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToMany(mappedBy = "theme")
    private Collection<ThemeCriteria> criteria;
    @OneToMany(mappedBy = "theme")
    private Collection<ThemeDeliveryRules> deliveryRules;
    @ManyToOne(fetch = FetchType.LAZY)
    private NotificationGroup notificationGroup;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Collection<ThemeCriteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(Collection<ThemeCriteria> criteria) {
        this.criteria = criteria;
    }

    public Collection<ThemeDeliveryRules> getDeliveryRules() {
        return deliveryRules;
    }

    public void setDeliveryRules(Collection<ThemeDeliveryRules> deliveryRules) {
        this.deliveryRules = deliveryRules;
    }

    public NotificationGroup getNotificationGroup() {
        return notificationGroup;
    }

    public void setNotificationGroup(NotificationGroup notificationGroup) {
        this.notificationGroup = notificationGroup;
    }
}
