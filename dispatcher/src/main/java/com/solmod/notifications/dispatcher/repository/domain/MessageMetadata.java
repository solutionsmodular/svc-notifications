package com.solmod.notifications.dispatcher.repository.domain;

import jakarta.persistence.*;

@Entity(name = "MessageMetadata")
public class MessageMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_delivery_id")
    private MessageDelivery messageDelivery;
    private String metadataKey;
    private String metadataValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageDelivery getMessageDelivery() {
        return messageDelivery;
    }

    public void setMessageDelivery(MessageDelivery messageDelivery) {
        this.messageDelivery = messageDelivery;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }
}
