package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

@Entity
public class EventCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private MessageTheme theme;
    @Column(name = "meta-key")
    private String key;
    @Column(name = "meta-key-value")
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageTheme getTheme() {
        return theme;
    }

    public void setTheme(MessageTheme theme) {
        this.theme = theme;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
