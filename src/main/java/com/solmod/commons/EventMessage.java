package com.solmod.commons;

import org.joda.time.DateTime;

import java.io.Serializable;

public class EventMessage implements Serializable {

    private String subject;
    private String verb;
    private DateTime eventDate;
    private String publisher; // Names the tenant or component
    private Serializable context;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public DateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(DateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Serializable getContext() {
        return context;
    }

    public void setContext(Serializable context) {
        this.context = context;
    }
}
