package com.solmod.notification.engine.domain;

import java.util.Map;

public class NotificationDeliveryMetadata extends Timestamped {
    private Long notificationDeliveryId;
    private Map<String, String> metadata;

    public Long getNotificationDeliveryId() {
        return notificationDeliveryId;
    }

    public void setNotificationDeliveryId(Long notificationDeliveryId) {
        this.notificationDeliveryId = notificationDeliveryId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
