package com.solmod.notification.admin.data;


import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.Map;
import java.util.Objects;

public class MessageTemplateSearchCriteria {

    private Long tenantId;
    private String eventSubject;
    private String eventVerb;
    private Map<String, Object> context;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getEventSubject() {
        return eventSubject;
    }

    public void setEventSubject(String eventSubject) {
        this.eventSubject = eventSubject;
    }

    public String getEventVerb() {
        return eventVerb;
    }

    public void setEventVerb(String eventVerb) {
        this.eventVerb = eventVerb;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        try {
            return ObjectUtils.stringify(this);
        } catch (StringifyException e) {
            return Objects.toString(this);
        }
    }
}
