package com.solmod.notifications.dispatcher.domain;

import lombok.Data;

@Data
public class SolCommunication {
    Long communicationId;
    Long messageTemplateId;
    String content;
    String recipient;
}
