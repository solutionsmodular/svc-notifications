package com.solmod.notification.domain;

import com.solmod.commons.StringifyException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.solmod.commons.ObjectUtils.flatten;

/**
 * Encapsulate all that's essentially needed for a notification, upon which many messages may be triggered
 * to deliver
 */
public class NotificationContext {
    private final Map<String, Object> eventContext = new HashMap<>();
    private final Set<String> minContext = new HashSet<>();
    private final Set<MessageTemplate> qualifyingMessageTemplates = new HashSet<>();

    public Map<String, Object> getEventContext() {
        return eventContext;
    }

    public Set<String> getMinContext() {
        return minContext;
    }

    public Set<MessageTemplate> getQualifyingMessageTemplates() {
        return qualifyingMessageTemplates;
    }

    public void addBuildContextParam(String key, String value) {
        eventContext.put(key, value);
    }

    public void addBuildContext(String contextKey, Object context) throws StringifyException {
        Map<String, Object> flattenedContext = flatten(context);
        eventContext.put(contextKey, flattenedContext);
    }

    public void addMinContextParam(String key) {
        minContext.add(key);
    }

    public void addQualifyingMessageTemplate(MessageTemplate template) {
        qualifyingMessageTemplates.add(template);
    }


}
