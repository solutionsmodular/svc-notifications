package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity(name = "MessageThemes")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String description;

    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private Set<ThemeCriteria> criteria;
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private Set<MessageTemplate> messageTemplates;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_group_id")
    private NotificationGroup notificationGroup;
    private Integer maxSend;
    private Integer resendInterval;

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

    public Set<ThemeCriteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(Set<ThemeCriteria> criteria) {
        this.criteria = criteria;
    }

    public NotificationGroup getNotificationGroup() {
        return notificationGroup;
    }

    public void setNotificationGroup(NotificationGroup notificationGroup) {
        this.notificationGroup = notificationGroup;
    }

    public Set<MessageTemplate> getMessageTemplates() {
        return messageTemplates;
    }

    public void setMessageTemplates(Set<MessageTemplate> messageTemplates) {
        this.messageTemplates = messageTemplates;
    }

    public Integer getMaxSend() {
        return maxSend;
    }

    public void setMaxSend(Integer maxSend) {
        this.maxSend = maxSend;
    }

    public Integer getResendInterval() {
        return resendInterval;
    }

    public void setResendInterval(Integer resendInterval) {
        this.resendInterval = resendInterval;
    }
}
