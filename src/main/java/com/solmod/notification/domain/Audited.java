package com.solmod.notification.domain;

import org.joda.time.DateTime;

import java.util.Objects;

public abstract class Audited extends Timestamped {
    protected DateTime modifiedDate;

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;

        if (this == o) return true;
        if (!(o instanceof Audited)) return false;

        Audited that = (Audited) o;

        return Objects.equals(modifiedDate, that.modifiedDate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        return result;
    }

}
