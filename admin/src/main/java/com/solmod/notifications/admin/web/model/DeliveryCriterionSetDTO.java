package com.solmod.notifications.admin.web.model;

import lombok.Data;

import java.util.*;

@Data
public class DeliveryCriterionSetDTO {
    private Map<String, String> criteria = new HashMap<>();

    public Map<String, String> getCriteria() {
        return criteria;
    }

    public void setCriteria(Map<String, String> criteria) {
        this.criteria = criteria;
    }

    public void addCriterion(String key, String value) {
        criteria.put(key, value);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeliveryCriterionSetDTO comp)) {
            return false;
        }

        return this.getCriteria().equals(comp.getCriteria());
    }
}
