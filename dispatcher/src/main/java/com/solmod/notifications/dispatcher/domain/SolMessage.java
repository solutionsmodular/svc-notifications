package com.solmod.notifications.dispatcher.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * On the SolBus, all messages follow this format
 */
@Data
public class SolMessage {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final Logger log = LoggerFactory.getLogger(SolMessage.class);

    private String subject;
    private String verb;
    private String idMetadataKey;
    private String idMetadataValue;
    private String publisher; // app/component
    private Long tenantId;
    private Long entityId;
    private Object data;
    @JsonIgnore
    private Map<String, Object> metadata;

    public Map<String, Object> buildMetadata() {
        try {
            if (this.metadata == null && data != null) {
                this.metadata = flatten(data);
            }
        } catch (JsonProcessingException e) {
            log.error("Exception attempting to get message metadata {}", e.getMessage(), e);
        }

        return this.metadata;
    }

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

}
