package com.solmod.notifications.dispatcher.domain;

import lombok.Data;

import java.util.Map;

@Data
public class TriggeringEvent {
    private  String subject;
    private String verb;
    private String subjectIdMetadataKey;
    private String subjectId;
    private Map<String, String> eventMetadata;
    private Long tenantId;
}
