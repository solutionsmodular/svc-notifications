package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.UUID;

@Entity
public class NotificationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToMany(mappedBy = "notificationGroup")
    private Collection<MessageTheme> messageThemes;
    private String subject;
    private String verb;
    private String description;
    private boolean active;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Collection<MessageTheme> getMessageThemes() {
        return messageThemes;
    }

    public void setMessageThemes(Collection<MessageTheme> messageThemes) {
        this.messageThemes = messageThemes;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
