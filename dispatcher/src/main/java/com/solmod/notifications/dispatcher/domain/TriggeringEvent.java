package com.solmod.notifications.dispatcher.domain;

import lombok.Data;

import java.util.Map;

@Data
public class TriggeringEvent {
    private Map<String, String> eventMetadata;
    private String subjectIdentifierMetadataKey;
}
