package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Collection;

@Entity(name = "MessageThemes")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String description;

    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private Collection<ThemeCriteria> criteria;
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private Collection<ThemeDeliveryRules> deliveryRules;
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private Collection<MessageTemplate> messageTemplates;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_group_id")
    private NotificationGroup notificationGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Collection<MessageTemplate> getMessageTemplates() {
        return messageTemplates;
    }

    public void setMessageTemplates(Collection<MessageTemplate> messageTemplates) {
        this.messageTemplates = messageTemplates;
    }
}
