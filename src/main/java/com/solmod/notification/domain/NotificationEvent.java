package com.solmod.notification.domain;

import com.solmod.commons.ObjectUtils;
import com.solmod.commons.StringifyException;

import java.util.Objects;

/**
 * The NotificationEvent is the first point of access when detecting which messages should be delivered based on
 * a message on the message bus. At its most fundamental, it singles out events by the subject/verb.
 */
public class NotificationEvent extends Tenanted {

    private String eventSubject;
    private String eventVerb;
    private Status status;

    public String getEventSubject() {
        return eventSubject;
    }

    public void setEventSubject(String eventSubject) {
        this.eventSubject = eventSubject;
    }

    public String getEventVerb() {
        return eventVerb;
    }

    public void setEventVerb(String eventVerb) {
        this.eventVerb = eventVerb;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!super.equals(o))
            return false;

        NotificationEvent that = (NotificationEvent) o;

        if (!Objects.equals(eventSubject, that.eventSubject)) return false;
        if (!Objects.equals(eventVerb, that.eventVerb)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += (eventSubject != null ? eventSubject.hashCode() : 0);
        result += (eventVerb != null ? eventVerb.hashCode() : 0);
        result += (status != null ? status.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        try {
            return ObjectUtils.stringify(this);
        } catch (StringifyException e) {
            return Objects.toString(this);
        }
    }
}
