package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
public class FilterResponse {
    private final String filterName;
    Map<Long, DeliveryPermission> permissions = new HashMap<>();

    public FilterResponse(String filterName) {
        this.filterName = filterName;
    }

    public FilterResponse addDeliveryPermission(Long templateId, DeliveryPermission permission) {
        this.permissions.put(templateId, permission);
        return this;
    }
}
