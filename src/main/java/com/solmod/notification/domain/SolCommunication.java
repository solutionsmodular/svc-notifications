package com.solmod.notification.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SolCommunication {
    Long communicationId;
    Long messageTemplateId;
    String content;
    String recipient;
}