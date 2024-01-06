package com.solmod.notifications.admin.web.model;

import java.util.HashSet;
import java.util.Set;

public class DeliveryCriterionSetDTO {
    private Set<DeliveryCriteriaDTO> criteria = new HashSet<>();

    public Set<DeliveryCriteriaDTO> getCriteria() {
        return criteria;
    }

    public void setCriteria(Set<DeliveryCriteriaDTO> criteria) {
        this.criteria = criteria;
    }

    public void addCriterion(DeliveryCriteriaDTO criteriaDTO) {
        criteria.add(criteriaDTO);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DeliveryCriterionSetDTO)) {
            return false;
        }

        DeliveryCriterionSetDTO comp = (DeliveryCriterionSetDTO) obj;

        return this.getCriteria().equals(comp.getCriteria());
    }
}
