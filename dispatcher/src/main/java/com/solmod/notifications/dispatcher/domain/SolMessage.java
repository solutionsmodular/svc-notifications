package com.solmod.notifications.dispatcher.domain;

import lombok.Data;

/**
 * On the SolBus, all messages follow this format
 */
@Data
public class SolMessage {
    private String subject;
    private String verb;
    private String idMetadataKey;
    private String idMetadataValue;
    private String publisher; // app/component
    private Long tenantId;
    private Long entityId;
    private Object data;
}
