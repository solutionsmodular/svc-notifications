package com.solmod.notification.domain.summary;

import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.Status;
import lombok.Data;

@Data
public class MessageTemplateSummary {
    private Long id;
    private Long messageConfigId;
    private Status mt_status;
    private String recipientContextKey;
    private MessageSender messageSender;
    private String contentKey;
}
