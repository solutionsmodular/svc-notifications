package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.*;

/**
 * Override delivery rules as set by the parent Theme, if any
 */
@Entity(name = "MessageTemplateDeliveryRules")
public class TemplateDeliveryRules {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private MessageTemplate template;
    private Integer maxSend;
    private Integer resendInterval;
    private Integer intervalPeriod; // Use Calendar constants

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
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
