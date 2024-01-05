package com.solmod.notifications.admin.web.model;

import java.util.List;

public class DeliveryCriteriaSetDTO {
    private List<DeliveryCriteriaDTO> rules;

    public List<DeliveryCriteriaDTO> getRules() {
        return rules;
    }

    public void setRules(List<DeliveryCriteriaDTO> rules) {
        this.rules = rules;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DeliveryCriteriaSetDTO)) {
            return false;
        }

        DeliveryCriteriaSetDTO comp = (DeliveryCriteriaSetDTO) obj;

        return this.getRules().equals(comp.getRules());
    }
}
