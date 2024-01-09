package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

import java.util.Collection;

/**
 * Group a collection of Message Themes for a given subject/verb
 */
@Entity(name = "NotificationGroups")
public class NotificationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long tenantId;

    @OneToMany(mappedBy = "notificationGroup", cascade = CascadeType.ALL)
    private Collection<Theme> themes;
    private String subject;
    private String verb;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Collection<Theme> getThemes() {
        return themes;
    }

    public void setThemes(Collection<Theme> themes) {
        this.themes = themes;
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
}
