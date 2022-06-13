package com.solmod.notification.domain;

import org.joda.time.DateTime;

import java.util.Objects;

public abstract class Audited extends BaseDomain {
    protected DateTime createdDate;
    protected DateTime modifiedDate;
    protected boolean isDeleted;

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;

        if (this == o) return true;
        if (!(o instanceof Audited)) return false;

        Audited that = (Audited) o;

        if (!createdDate.equals(that.createdDate)) return false;
        return Objects.equals(modifiedDate, that.modifiedDate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result + (createdDate != null ? createdDate.hashCode() : 0);
        result = result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        return result;
    }

}
