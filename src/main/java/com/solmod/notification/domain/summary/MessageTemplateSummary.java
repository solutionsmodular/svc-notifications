package com.solmod.notification.domain.summary;

import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.Status;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class MessageTemplateSummary {
    private Long id;
    private Long messageConfigId;
    private Status mt_status;
    private String recipientContextKey;
    private MessageSender messageSender;
    private String contentKey;
}
