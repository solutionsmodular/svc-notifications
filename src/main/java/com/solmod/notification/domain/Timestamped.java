package com.solmod.notification.domain;

import org.joda.time.DateTime;

import java.util.Objects;

public abstract class Timestamped extends BaseDomain {
    protected DateTime createdDate;

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;

        if (this == o) return true;
        if (!(o instanceof Timestamped)) return false;

        Timestamped that = (Timestamped) o;

        return (Objects.equals(createdDate, that.createdDate));
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result + (createdDate != null ? createdDate.hashCode() : 0);
        return result;
    }

}
