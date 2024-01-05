package com.solmod.notifications.admin.web.model;

public class DeliveryRulesDTO {
    private Integer maxSend;
    private Integer resendInterval;
    private Integer intervalPeriod; // Use Calendar constants

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
