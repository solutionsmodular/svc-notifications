package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

@Entity
@Table(name = "MessageThemeCriteria")
public class ThemeCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_theme_id")
    private Theme theme;
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

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
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
