package com.solmod.notifications.admin.web.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * The content key set will provide the content block keys for different parts of different types of message templates.
 * For instance, email messages have an extra content key needed to define the email subject, so different template
 * types will have different needs for content blocks.
 * All content key sets will include a content key for message body. Keys match member var name
 * for the particular MessageTemplate type.
 */
@Data
public class ContentKeySetDTO {
    private Map<String, String> contentKeys;

    public void addContentKey(String contentKeyName, String contentKeyMetadata) {
        if (contentKeys == null) {
            contentKeys = new HashMap<>();
        }

        contentKeys.put(contentKeyName, contentKeyMetadata);
    }
}
