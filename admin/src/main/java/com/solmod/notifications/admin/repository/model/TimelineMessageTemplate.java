package com.solmod.notifications.admin.repository.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity(name = "TimelineMessageTemplates")
@DiscriminatorValue("T")
public class TimelineMessageTemplate extends MessageTemplate {

    @Enumerated(EnumType.STRING)
    private TimelineNodeType timelineNodeType;
    private String nodeTitleContentKey;

    public enum TimelineNodeType {
        TIMELINE, ALERT, COMMUNITY // TODO: These should be config and tenanted
    }

    public TimelineNodeType getTimelineNodeType() {
        return timelineNodeType;
    }

    public void setTimelineNodeType(TimelineNodeType timelineNodeType) {
        this.timelineNodeType = timelineNodeType;
    }

    public String getNodeTitleContentKey() {
        return nodeTitleContentKey;
    }

    public void setNodeTitleContentKey(String nodeTitleContentKey) {
        nodeTitleContentKey = nodeTitleContentKey;
    }
}
