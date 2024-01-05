package com.solmod.notifications.admin.web.model;

import java.util.List;

public class TemplateRuleset {
    private List<TemplateRule> rules;

    public List<TemplateRule> getRules() {
        return rules;
    }

    public void setRules(List<TemplateRule> rules) {
        this.rules = rules;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TemplateRuleset)) {
            return false;
        }

        TemplateRuleset comp = (TemplateRuleset) obj;

        return this.getRules().equals(comp.getRules());
    }
}
