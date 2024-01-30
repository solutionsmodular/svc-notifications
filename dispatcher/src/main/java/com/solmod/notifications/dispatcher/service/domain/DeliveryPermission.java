package com.solmod.notifications.dispatcher.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeliveryPermission {

    public static DeliveryPermission SEND_NOW_PERMISSION = new DeliveryPermission(Verdict.SEND_NOW, "");

    private Verdict verdict;
    private String message;

    public enum Verdict {
        SEND_NOW, SEND_LATER, SEND_NEVER
    }

    public DeliveryPermission(Verdict verdict) {
        this.verdict = verdict;
    }
}
