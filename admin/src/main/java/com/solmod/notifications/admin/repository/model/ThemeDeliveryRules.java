package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

@Entity(name = "MessageThemeDeliveryRules")
public class ThemeDeliveryRules {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_theme_id")
    private Theme theme;
    private Integer maxSend;
    private Integer resendInterval;
    private Integer intervalPeriod; // Use Calendar constants

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

    public Integer getIntervalPeriod() {
        return intervalPeriod;
    }

    public void setIntervalPeriod(Integer intervalPeriod) {
        this.intervalPeriod = intervalPeriod;
    }
}
