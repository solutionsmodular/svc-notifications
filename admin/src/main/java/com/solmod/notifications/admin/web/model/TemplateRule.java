package com.solmod.notifications.admin.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRule {
    private String key;
    private String value;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TemplateRule)) {
            return false;
        }

        TemplateRule comp = (TemplateRule) obj;
        return key != null && key.equals(comp.getKey()) &&
            value != null && value.equals(comp.getValue());
    }
}
