package com.solmod.notifications.dispatcher.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * On the SolBus, all messages follow this format
 */
public class SolMessage {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final Logger log = LoggerFactory.getLogger(SolMessage.class);

    private String subject;
    private String verb;
    private String idMetadataKey;
    private String publisher; // app/component
    private Long tenantId;
    private Long entityId;
    private Object data;
    private Map<String, Object> metadata;

    /**
     * Create a flat, Properties-like, construct representing the data in the provided context. This facilitates
     * the use of context keys such as {@code parent.child.property}
     *
     * @param context {@code Object} of any sort, to flatten
     * @return Map of String key Object value context
     * @throws JsonProcessingException In the event there's something funky with the specified context
     */
    private Map<String, Object> flatten(Object context) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(context);

        return JsonFlattener.flattenAsMap(json);
    }

    private Map<String, Object> buildMetadata() {
        try {
            if (this.metadata == null && data != null) {
                this.metadata = flatten(data);
            }
        } catch (JsonProcessingException e) {
            log.error("Exception attempting to get message metadata {}", e.getMessage(), e);
        }

        return this.metadata;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getIdMetadataKey() {
        return idMetadataKey;
    }

    public void setIdMetadataKey(String idMetadataKey) {
        this.idMetadataKey = idMetadataKey;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getMetadata() {
        return metadata == null ? buildMetadata() : metadata;
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }
}
