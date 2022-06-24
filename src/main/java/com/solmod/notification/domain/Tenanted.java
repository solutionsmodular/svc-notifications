package com.solmod.notification.domain;

import java.util.Objects;

public abstract class Tenanted extends Audited {
    private Long tenantId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;

        if (this == o) return true;
        if (!(o instanceof Tenanted)) return false;

        Tenanted that = (Tenanted) o;

        return Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }
}
