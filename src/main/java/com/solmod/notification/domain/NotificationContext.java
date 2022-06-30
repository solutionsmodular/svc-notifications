package com.solmod.notification.domain;

import java.util.Map;

/**
 * Encapsulate all that's essentially needed for a notification, upon which many messages may be triggered
 * to deliver
 */
public class NotificationContext {
    private final Map<String, String> builtContext;
    private final Map<String, String> minContext;

    public NotificationContext(Map<String, String> builtContext, Map<String, String> minContext) {
        this.builtContext = builtContext;
        this.minContext = minContext;
    }

    public Map<String, String> getBuiltContext() {
        return builtContext;
    }

    public Map<String, String> getMinContext() {
        return minContext;
    }
}
