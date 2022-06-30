package com.solmod.notification.domain;

/**
 * A message channel is one through which a notification can be delivered.
 * Dispatcher, upon receipt of an event will search0
 *
 */
public class MessageChannel {
    enum OwnerType {
        TENANT, USER, TEAM
    }

    private OwnerType ownerType;
    private String channelName;
    private Status status;
    private SenderStrategy sender;

}

